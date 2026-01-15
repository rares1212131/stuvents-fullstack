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

    @Mock
    private OrganizerApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void approveApplication_ShouldAddOrganizerRoleToUserAndSetStatusToApproved() {


        Role userRole = new Role("ROLE_USER");
        User applicantUser = new User();
        applicantUser.setId(1L);
        applicantUser.setRoles(new HashSet<>(Set.of(userRole)));

        OrganizerApplication pendingApplication = new OrganizerApplication();
        pendingApplication.setId(100L);
        pendingApplication.setUser(applicantUser);
        pendingApplication.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));

        doNothing().when(userService).setUserRoles(anyLong(), anyList());

        applicationService.approveApplication(100L);


        verify(userService, times(1)).setUserRoles(anyLong(), anyList());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<List<String>> rolesListCaptor = ArgumentCaptor.forClass(List.class);

        verify(userService).setUserRoles(userIdCaptor.capture(), rolesListCaptor.capture());

        assertEquals(1L, userIdCaptor.getValue());
        List<String> capturedRoles = rolesListCaptor.getValue();
        assertTrue(capturedRoles.contains("ROLE_USER"));
        assertTrue(capturedRoles.contains("ROLE_ORGANIZER"));

        ArgumentCaptor<OrganizerApplication> applicationCaptor = ArgumentCaptor.forClass(OrganizerApplication.class);
        verify(applicationRepository).save(applicationCaptor.capture());

        assertEquals(ApplicationStatus.APPROVED, applicationCaptor.getValue().getStatus());
    }
}