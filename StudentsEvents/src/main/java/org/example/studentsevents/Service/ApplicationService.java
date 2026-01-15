package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.OrganizerApplicationRequest;
import org.example.studentsevents.DTOResponse.AdminApplicationResponse;
import org.example.studentsevents.DTOResponse.OrganizerApplicationResponse;
import org.example.studentsevents.Repository.OrganizerApplicationRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.ApplicationStatus;
import org.example.studentsevents.model.OrganizerApplication;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final OrganizerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public void submitApplication(Long userId, OrganizerApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (applicationRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("You have already submitted an application.");
        }

        OrganizerApplication application = new OrganizerApplication();
        application.setUser(user);
        application.setReason(request.getReason());
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public OrganizerApplicationResponse getApplicationStatusForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return applicationRepository.findByUser(user)
                .map(app -> new OrganizerApplicationResponse(app.getId(), app.getStatus(), app.getReason()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> getPendingApplications() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING).stream()
                .map(app -> new AdminApplicationResponse(
                        app.getId(),
                        app.getStatus(),
                        app.getReason(),
                        app.getUser().getId(),
                        app.getUser().getFirstName(),
                        app.getUser().getLastName(),
                        app.getUser().getEmail()
                ))
                .collect(Collectors.toList());
    }

    public void approveApplication(Long applicationId) {
        OrganizerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        User user = application.getUser();

        List<String> currentRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        if (!currentRoleNames.contains("ROLE_ORGANIZER")) {
            currentRoleNames.add("ROLE_ORGANIZER");
        }

        userService.setUserRoles(user.getId(), currentRoleNames);

        application.setStatus(ApplicationStatus.APPROVED);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    @Transactional
    public void denyApplication(Long applicationId) {
        OrganizerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        application.setStatus(ApplicationStatus.DENIED);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }
}