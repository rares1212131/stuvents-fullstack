package org.example.studentsevents.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.OrganizerApplicationRequest;
import org.example.studentsevents.DTOResponse.OrganizerApplicationResponse;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.Service.ApplicationService;
import org.example.studentsevents.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/organizer-applications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;
    @PostMapping
    public ResponseEntity<Void> submitApplication(@Valid @RequestBody OrganizerApplicationRequest request) {
        User currentUser = getCurrentUser();
        applicationService.submitApplication(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-status")
    public ResponseEntity<OrganizerApplicationResponse> getMyApplicationStatus() {
        User currentUser = getCurrentUser();
        OrganizerApplicationResponse response = applicationService.getApplicationStatusForUser(currentUser.getId());

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database. This should not happen."));
    }
}