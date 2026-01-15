package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.BookingRequest;
import org.example.studentsevents.DTOResponse.BookingResponse;
import org.example.studentsevents.Repository.BookingRepository;
import org.example.studentsevents.Repository.TicketTypeRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Booking;
import org.example.studentsevents.model.TicketType;
import org.example.studentsevents.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void createBooking(BookingRequest bookingRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        TicketType ticketType = ticketTypeRepository.findById(bookingRequest.getTicketTypeId())
                .orElseThrow(() -> new RuntimeException("TicketType not found with id: " + bookingRequest.getTicketTypeId()));

        Integer quantityToBook = bookingRequest.getQuantity();
        if (quantityToBook == null || quantityToBook <= 0) {
            throw new IllegalStateException("You must specify a quantity greater than zero.");
        }

        long ticketsSold = bookingRepository.countByTicketTypeId(ticketType.getId());
        if ((ticketsSold + quantityToBook) > ticketType.getTotalAvailable()) {
            long available = ticketType.getTotalAvailable() - ticketsSold;
            throw new IllegalStateException("Sorry, only " + available + " tickets are left, but you requested " + quantityToBook + ".");
        }

        for (int i = 0; i < quantityToBook; i++) {
            Booking newBooking = new Booking();
            newBooking.setUser(user);
            newBooking.setTicketType(ticketType);
            newBooking.setBookingDateTime(LocalDateTime.now());

            user.getBookings().add(newBooking);
            ticketType.getBookings().add(newBooking);

            bookingRepository.save(newBooking);
        }
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        Page<Booking> userBookings = bookingRepository.findBookingsForUserWithDetails(user, pageable);

        return userBookings.map(booking -> modelMapper.map(booking, BookingResponse.class));
    }
    @Transactional(readOnly = true)
    public BookingResponse getMyBookingById(Long bookingId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        Booking booking = bookingRepository.findByIdAndUser(bookingId, user)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId + " for the current user."));

        return modelMapper.map(booking, BookingResponse.class);
    }
}