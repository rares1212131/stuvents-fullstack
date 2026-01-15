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
        bookingRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User organizer = new User();
        organizer.setEmail("organizer@test.com");
        organizer.setPassword("password");
        organizer.setFirstName("Test");
        organizer.setLastName("Organizer");
        organizer.setVerified(true);
        User savedOrganizer = userRepository.saveAndFlush(organizer);

        Event event = new Event();
        event.setName("Concurrency Test Event");
        event.setDescription("Desc");
        event.setAddress("Addr");
        event.setEventDateTime(java.time.LocalDateTime.now().plusDays(1));
        event.setOrganizer(savedOrganizer);

        TicketType ticketType = new TicketType();
        ticketType.setName("Last Ticket");
        ticketType.setPrice(BigDecimal.TEN);
        ticketType.setTotalAvailable(1);
        ticketType.setEvent(event);

        event.getTicketTypes().add(ticketType);

        Event savedEvent = eventRepository.saveAndFlush(event);
        lastTicketTypeId = savedEvent.getTicketTypes().get(0).getId();
    }

    @Test
    @WithMockUser("test@user.com")
    @Transactional(propagation = Propagation.NEVER)
    void createBooking_ShouldPassWithPessimisticLock() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        BookingRequest request = new BookingRequest();
        request.setTicketTypeId(lastTicketTypeId);
        request.setQuantity(1);

        User buyer = new User();
        buyer.setEmail("test@user.com");
        buyer.setPassword("password");
        buyer.setFirstName("Test");
        buyer.setLastName("Buyer");
        buyer.setVerified(true);
        userRepository.save(buyer);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        for (int i = 0; i < numberOfThreads; i++) {
            Runnable task = () -> {
                try {
                    bookingService.createBooking(request);
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            };

            Runnable taskWithSecurityContext = new DelegatingSecurityContextRunnable(task, securityContext);

            executorService.submit(taskWithSecurityContext);
        }

        latch.await();
        executorService.shutdown();

        long bookingsInDb = bookingRepository.count();

        System.out.println("Total bookings in DB: " + bookingsInDb);
        System.out.println("Number of exceptions: " + exceptions.size());

        assertEquals(1, bookingsInDb, "Database should only contain one successful booking.");
        assertEquals(1, exceptions.size(), "One of the booking attempts should have thrown an exception.");
    }
}