package org.example.studentsevents.Service;

import org.example.studentsevents.DTORequest.BookingRequest;
import org.example.studentsevents.Repository.BookingRepository;
import org.example.studentsevents.Repository.EventRepository;
import org.example.studentsevents.Repository.TicketTypeRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Event;
import org.example.studentsevents.model.TicketType;
import org.example.studentsevents.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookingServiceConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Long lastTicketTypeId;

    @BeforeEach
    @Transactional
    void setUpDatabase() {
        // Clear repositories in an order that respects foreign key constraints
        bookingRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create and save a User to act as the event Organizer
        User organizer = new User();
        organizer.setEmail("organizer@test.com");
        organizer.setPassword("password");
        organizer.setFirstName("Test");
        organizer.setLastName("Organizer");
        organizer.setVerified(true);
        User savedOrganizer = userRepository.saveAndFlush(organizer);

        // 2. Create the Event and assign the organizer
        Event event = new Event();
        event.setName("Concurrency Test Event");
        event.setDescription("Desc");
        event.setAddress("Addr");
        event.setEventDateTime(java.time.LocalDateTime.now().plusDays(1));
        event.setOrganizer(savedOrganizer);

        // 3. Create the TicketType with only one ticket available
        TicketType ticketType = new TicketType();
        ticketType.setName("Last Ticket");
        ticketType.setPrice(BigDecimal.TEN);
        ticketType.setTotalAvailable(1); // CRITICAL: Only one ticket
        ticketType.setEvent(event);

        event.getTicketTypes().add(ticketType);

        // 4. Save the event, which cascades to save the ticket type
        Event savedEvent = eventRepository.saveAndFlush(event);
        lastTicketTypeId = savedEvent.getTicketTypes().get(0).getId();
    }

    @Test
    @WithMockUser("test@user.com") // Sets up the SecurityContext on the main test thread
    @Transactional(propagation = Propagation.NEVER) // Disables test-wide transaction to allow for real concurrency
    void createBooking_ShouldPassWithPessimisticLock() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Use a thread-safe list to collect exceptions from worker threads
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(lastTicketTypeId);
        request.setQuantity(1);

        // The BookingService needs to find the user in the database.
        // We must create and save a user that matches the @WithMockUser email.
        User buyer = new User();
        buyer.setEmail("test@user.com");
        buyer.setPassword("password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        buyer.setVerified(true);
        userRepository.save(buyer);

        // Get the security context from the main thread to pass it to the worker threads.
        SecurityContext securityContext = SecurityContextHolder.getContext();

        for (int i = 0; i < numberOfThreads; i++) {
            // Define the core task to be executed by the thread
            Runnable task = () -> {
                try {
                    // This is where the race condition happens
                    bookingService.createBooking(request);
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            };

            // Wrap the task to ensure the security context is propagated to the new thread
            Runnable taskWithSecurityContext = new DelegatingSecurityContextRunnable(task, securityContext);

            // Submit the wrapped, security-aware task for execution
            executorService.submit(taskWithSecurityContext);
        }

        // Wait for both threads to complete their work
        latch.await();
        executorService.shutdown();

        // --- ASSERT THE FINAL STATE ---
        // Check the database directly to see the result of the race.
        long bookingsInDb = bookingRepository.count();

        // Print the results for clarity
        System.out.println("Total bookings in DB: " + bookingsInDb);
        System.out.println("Number of exceptions: " + exceptions.size());

        // The successful outcome: one thread succeeded, one failed with an exception.
        assertEquals(1, bookingsInDb, "Database should only contain one successful booking.");
        assertEquals(1, exceptions.size(), "One of the booking attempts should have thrown an exception.");
    }
}