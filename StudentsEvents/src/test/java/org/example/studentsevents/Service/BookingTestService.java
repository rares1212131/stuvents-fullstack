package org.example.studentsevents.Service;

import org.example.studentsevents.DTORequest.BookingRequest;
import org.example.studentsevents.Repository.BookingRepository;
import org.example.studentsevents.Repository.TicketTypeRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Booking;
import org.example.studentsevents.model.TicketType;
import org.example.studentsevents.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTestService {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createBooking_ShouldThrowException_WhenTicketsAreSoldOut() {
        when(authentication.getName()).thenReturn("test@user.com");

        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(1L);
        request.setQuantity(1);

        User fakeUser = new User();
        TicketType soldOutTicket = new TicketType();
        soldOutTicket.setId(1L);
        soldOutTicket.setTotalAvailable(50);

        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(soldOutTicket));

        when(bookingRepository.countByTicketTypeId(1L)).thenReturn(50L);

        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ShouldSucceed_WhenTicketsAreAvailable() {
        when(authentication.getName()).thenReturn("test@user.com");

        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(1L);
        request.setQuantity(2);

        User fakeUser = new User();
        TicketType availableTicket = new TicketType();
        availableTicket.setId(1L);
        availableTicket.setTotalAvailable(50);

        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(availableTicket));

        when(bookingRepository.countByTicketTypeId(1L)).thenReturn(10L);

        bookingService.createBooking(request);

        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void getMyBookingById_ShouldThrowException_WhenUserDoesNotOwnBooking() {
        when(authentication.getName()).thenReturn("test@user.com");

        long bookingIdTheyAreRequesting = 123L;
        User fakeUser = new User();

        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));

        when(bookingRepository.findByIdAndUser(bookingIdTheyAreRequesting, fakeUser))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            bookingService.getMyBookingById(bookingIdTheyAreRequesting);
        });
    }
}