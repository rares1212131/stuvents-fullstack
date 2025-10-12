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

// This annotation enables Mockito for this test class.
@ExtendWith(MockitoExtension.class)
class BookingTestService {

    // @Mock creates a fake, "mocked" version of a dependency.
    // These mocks do NOT connect to the database. They do whatever we tell them to.
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;

    // We need to mock the security objects to simulate a logged-in user.
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    // @InjectMocks creates a REAL instance of BookingService and automatically
    // "injects" the mocked dependencies from above into it.
    @InjectMocks
    private BookingService bookingService;

    // This method runs before each test, setting up the security context.
    @BeforeEach
    void setUp() {
        // Tell the SecurityContextHolder to use our mock context.
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createBooking_ShouldThrowException_WhenTicketsAreSoldOut() {
        // --- ARRANGE ---
        // We set up the scenario and tell our mocks how to behave.

        // 1. Simulate a logged-in user with the email "test@user.com".
        when(authentication.getName()).thenReturn("test@user.com");

        // 2. Create the request that would come from the frontend.
        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(1L);
        request.setQuantity(1); // The user wants to buy 1 ticket.

        // 3. Create fake data objects.
        User fakeUser = new User();
        TicketType soldOutTicket = new TicketType();
        soldOutTicket.setId(1L);
        soldOutTicket.setTotalAvailable(50); // There are 50 tickets in total.

        // 4. Define the behavior of our mocked repositories.
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(soldOutTicket));

        // THIS IS THE KEY to the test:
        // When the service asks "how many tickets are sold?", we tell it "50".
        // Since TotalAvailable is 50 and 50 are sold, there are 0 left.
        when(bookingRepository.countByTicketTypeId(1L)).thenReturn(50L);


        // --- ACT & ASSERT ---
        // We perform the action and check if the result is what we expect.

        // We expect that calling createBooking with this setup will throw an IllegalStateException.
        // assertThrows will "catch" the exception. If the correct exception is thrown, the test passes.
        // If no exception or a different one is thrown, the test fails.
        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ShouldSucceed_WhenTicketsAreAvailable() {
        // --- ARRANGE ---
        // We set up a scenario where the booking SHOULD work.

        // 1. Simulate a logged-in user.
        when(authentication.getName()).thenReturn("test@user.com");

        // 2. Create the request.
        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(1L);
        request.setQuantity(2); // User wants to buy 2 tickets.

        // 3. Create fake data.
        User fakeUser = new User();
        TicketType availableTicket = new TicketType();
        availableTicket.setId(1L);
        availableTicket.setTotalAvailable(50); // 50 tickets total.

        // 4. Define mock behavior.
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(availableTicket));

        // THIS IS THE KEY DIFFERENCE:
        // When the service asks "how many are sold?", we tell it only "10" are sold.
        when(bookingRepository.countByTicketTypeId(1L)).thenReturn(10L);

        // The check will be: (10 sold) + (2 new) > (50 total) -> 12 > 50, which is FALSE.
        // So, no exception should be thrown.

        // --- ACT ---
        // We call the method we want to test.
        bookingService.createBooking(request);


        // --- ASSERT ---
        // The most important assertion here is to verify that the `save` method
        // was called, and it was called exactly 2 times (because the user bought 2 tickets).
        // `verify` is a Mockito function that checks for method invocations.
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void getMyBookingById_ShouldThrowException_WhenUserDoesNotOwnBooking() {
        // --- ARRANGE ---
        // We set up a scenario where the user is trying to access a booking they don't own.

        // 1. Simulate a logged-in user.
        when(authentication.getName()).thenReturn("test@user.com");

        long bookingIdTheyAreRequesting = 123L;
        User fakeUser = new User(); // The currently logged-in user.

        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(fakeUser));

        // THIS IS THE KEY to the security test:
        // The service will call bookingRepository.findByIdAndUser(123L, fakeUser).
        // We tell the mock repository to return "Optional.empty()". This simulates the
        // database finding no booking that has BOTH that ID AND is owned by that user.
        when(bookingRepository.findByIdAndUser(bookingIdTheyAreRequesting, fakeUser))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // We expect the .orElseThrow() in the service to trigger, protecting the data.
        assertThrows(RuntimeException.class, () -> {
            bookingService.getMyBookingById(bookingIdTheyAreRequesting);
        });
    }
}