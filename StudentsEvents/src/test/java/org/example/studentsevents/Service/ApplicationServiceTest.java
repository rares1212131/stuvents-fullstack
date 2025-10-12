package org.example.studentsevents.Service;

import org.example.studentsevents.Repository.OrganizerApplicationRepository;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.ApplicationStatus;
import org.example.studentsevents.model.OrganizerApplication;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    // --- Mocks for the dependencies ---
    @Mock
    private OrganizerApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService; // This is a dependency of ApplicationService
    @Mock
    private RoleRepository roleRepository; // This is a dependency of UserService, which we will bypass

    // --- The class we are testing ---
    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void approveApplication_ShouldAddOrganizerRoleToUserAndSetStatusToApproved() {
        // --- ARRANGE ---
        // Set up all the fake data and mock behaviors.

        // 1. Create a fake user who is currently just a regular user.
        Role userRole = new Role("ROLE_USER");
        User applicantUser = new User();
        applicantUser.setId(1L);
        applicantUser.setRoles(new HashSet<>(Set.of(userRole))); // Start with only ROLE_USER

        // 2. Create a fake "pending" application associated with that user.
        OrganizerApplication pendingApplication = new OrganizerApplication();
        pendingApplication.setId(100L);
        pendingApplication.setUser(applicantUser);
        pendingApplication.setStatus(ApplicationStatus.PENDING);

        // 3. Define the behavior of our mocked repositories.
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));

        // We need to tell the userService mock what to do. Since approveApplication calls
        // userService.setUserRoles, we can use `doNothing()` to simply allow that call
        // to happen without it doing anything, or we can mock its behavior more deeply.
        // For this test, we can actually let the real logic flow by using a spy or by
        // mocking the dependencies of UserService. Let's simplify by directly verifying
        // what approveApplication does. We will refactor to use the UserService's call.

        // Let's refine the test to verify the call to setUserRoles directly.
        // This makes the test simpler and more focused on ApplicationService's responsibility.
        doNothing().when(userService).setUserRoles(anyLong(), anyList());


        // --- ACT ---
        // Call the method we want to test.
        applicationService.approveApplication(100L);


        // --- ASSERT ---
        // Verify that the outcomes are what we expected.

        // 1. Verify that the `setUserRoles` method on our mock `userService` was called exactly once.
        verify(userService, times(1)).setUserRoles(anyLong(), anyList());

        // 2. We can use an ArgumentCaptor to capture the arguments passed to setUserRoles.
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<List<String>> rolesListCaptor = ArgumentCaptor.forClass(List.class);

        // Capture the arguments from the actual call that happened during ACT.
        verify(userService).setUserRoles(userIdCaptor.capture(), rolesListCaptor.capture());

        // Now, let's inspect the captured values.
        assertEquals(1L, userIdCaptor.getValue()); // Was it called for the correct user ID?
        List<String> capturedRoles = rolesListCaptor.getValue();
        assertTrue(capturedRoles.contains("ROLE_USER")); // Did it keep the original role?
        assertTrue(capturedRoles.contains("ROLE_ORGANIZER")); // Was the new role added?

        // 3. Verify that the application's status was updated to APPROVED.
        // We can use another captor for the application object.
        ArgumentCaptor<OrganizerApplication> applicationCaptor = ArgumentCaptor.forClass(OrganizerApplication.class);
        verify(applicationRepository).save(applicationCaptor.capture());

        assertEquals(ApplicationStatus.APPROVED, applicationCaptor.getValue().getStatus());
    }
}