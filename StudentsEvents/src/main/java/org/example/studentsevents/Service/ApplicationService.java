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

/**
 * Service to manage the business logic for organizer applications.
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final OrganizerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserService userService; // We inject UserService to handle role changes

    /**
     * Handles the logic for a user submitting a new application.
     * Corresponds to: POST /api/organizer-applications
     */
    @Transactional
    public void submitApplication(Long userId, OrganizerApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Prevent users from submitting more than one application
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

    /**
     * Retrieves the status of an application for the user who submitted it.
     * Corresponds to: GET /api/organizer-applications/my-status
     */
    @Transactional(readOnly = true)
    public OrganizerApplicationResponse getApplicationStatusForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Find the application and map it to the response DTO
        return applicationRepository.findByUser(user)
                .map(app -> new OrganizerApplicationResponse(app.getId(), app.getStatus(), app.getReason()))
                .orElse(null); // Returns null if no application is found for this user
    }

    /**
     * Retrieves all PENDING applications for the admin to review.
     * Corresponds to: GET /api/admin/organizer-applications
     */
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

    /**
     * Approves an application, which promotes the user to an organizer.
     * Corresponds to: POST /api/admin/organizer-applications/{id}/approve
     */
    public void approveApplication(Long applicationId) {
        OrganizerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        User user = application.getUser();

        // 1. Get the user's current roles
        List<String> currentRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // 2. Add the new role, ensuring no duplicates
        if (!currentRoleNames.contains("ROLE_ORGANIZER")) {
            currentRoleNames.add("ROLE_ORGANIZER");
        }

        // 3. Use the new, powerful setUserRoles method to update the user
        userService.setUserRoles(user.getId(), currentRoleNames);

        // 4. Update the application status
        application.setStatus(ApplicationStatus.APPROVED);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    /**
     * Denies an application.
     * Corresponds to: POST /api/admin/organizer-applications/{id}/deny
     */
    @Transactional
    public void denyApplication(Long applicationId) {
        OrganizerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        application.setStatus(ApplicationStatus.DENIED);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }
}