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

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;


    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findAndVerifyOwnership_ShouldThrowException_WhenUserIsNotOwnerOrAdmin() {
        User attackerUser = new User();
        attackerUser.setId(1L);
        attackerUser.setEmail("attacker@user.com");
        attackerUser.setRoles(Set.of(new Role("ROLE_ORGANIZER")));

        User ownerUser = new User();
        ownerUser.setId(2L);

        Event targetEvent = new Event();
        targetEvent.setId(100L);
        targetEvent.setOrganizer(ownerUser);

        when(authentication.getName()).thenReturn("attacker@user.com");
        when(userRepository.findByEmail("attacker@user.com")).thenReturn(Optional.of(attackerUser));

        when(eventRepository.findById(100L)).thenReturn(Optional.of(targetEvent));

        assertThrows(IllegalStateException.class, () -> {
            eventService.deleteEventForOrganizer(100L);
        });
    }

    @Test
    void findAndVerifyOwnership_ShouldNotThrowException_WhenUserIsAdmin() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setEmail("admin@user.com");
        adminUser.setRoles(Set.of(new Role("ROLE_ADMIN")));

        User ownerUser = new User();
        ownerUser.setId(2L);

        Event targetEvent = new Event();
        targetEvent.setId(100L);
        targetEvent.setOrganizer(ownerUser);

        when(authentication.getName()).thenReturn("admin@user.com");
        when(userRepository.findByEmail("admin@user.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(targetEvent));

        eventService.deleteEventForOrganizer(100L);
    }
}