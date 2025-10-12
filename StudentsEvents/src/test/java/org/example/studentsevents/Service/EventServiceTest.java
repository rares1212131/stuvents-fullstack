package org.example.studentsevents.Service;

import org.example.studentsevents.Repository.EventRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Event;
import org.example.studentsevents.model.Role;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    // --- Mocks for the dependencies ---
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    // We don't need mocks for the other dependencies for this specific test.

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    // --- The class we are testing ---
    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findAndVerifyOwnership_ShouldThrowException_WhenUserIsNotOwnerOrAdmin() {
        // --- ARRANGE ---
        // We set up the scenario where User A tries to access User B's event.

        // 1. Define the two different users.
        User attackerUser = new User(); // The user currently logged in
        attackerUser.setId(1L);
        attackerUser.setEmail("attacker@user.com");
        attackerUser.setRoles(Set.of(new Role("ROLE_ORGANIZER"))); // They are an organizer, but not the owner.

        User ownerUser = new User(); // The legitimate owner of the event
        ownerUser.setId(2L);

        // 2. Define the event that is owned by 'ownerUser'.
        Event targetEvent = new Event();
        targetEvent.setId(100L);
        targetEvent.setOrganizer(ownerUser); // This event belongs to User #2

        // 3. Define the behavior of our mocks.
        //    Simulate that 'attackerUser' is the one who is logged in.
        when(authentication.getName()).thenReturn("attacker@user.com");
        when(userRepository.findByEmail("attacker@user.com")).thenReturn(Optional.of(attackerUser));

        //    When the service asks to find the event by its ID, return the event owned by 'ownerUser'.
        when(eventRepository.findById(100L)).thenReturn(Optional.of(targetEvent));


        // --- ACT & ASSERT ---
        // We will call the delete method, but any method that uses the ownership check would work.
        // We expect the 'findAndVerifyOwnership' helper method to trigger and throw an IllegalStateException.
        assertThrows(IllegalStateException.class, () -> {
            eventService.deleteEventForOrganizer(100L);
        });
    }

    @Test
    void findAndVerifyOwnership_ShouldNotThrowException_WhenUserIsAdmin() {
        // --- ARRANGE ---
        // We set up a scenario where an ADMIN tries to access User B's event.

        // 1. Define the users.
        User adminUser = new User(); // The user currently logged in is an ADMIN.
        adminUser.setId(99L);
        adminUser.setEmail("admin@user.com");
        adminUser.setRoles(Set.of(new Role("ROLE_ADMIN"))); // This is the key difference.

        User ownerUser = new User(); // The owner of the event.
        ownerUser.setId(2L);

        // 2. Define the event owned by 'ownerUser'.
        Event targetEvent = new Event();
        targetEvent.setId(100L);
        targetEvent.setOrganizer(ownerUser);

        // 3. Define mock behavior.
        when(authentication.getName()).thenReturn("admin@user.com");
        when(userRepository.findByEmail("admin@user.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(targetEvent));

        // --- ACT & ASSERT ---
        // We call the method, but this time we expect NO exception to be thrown
        // because the admin role should bypass the ownership check.
        // If an exception is thrown, this test will fail.
        eventService.deleteEventForOrganizer(100L);
    }
}