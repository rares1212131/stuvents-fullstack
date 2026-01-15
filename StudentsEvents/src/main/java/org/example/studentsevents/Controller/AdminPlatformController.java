package org.example.studentsevents.Controller;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTOResponse.AdminApplicationResponse;
import org.example.studentsevents.DTOResponse.AdminEventResponse;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Service.ApplicationService;
import org.example.studentsevents.Service.EventService;
import org.example.studentsevents.Service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlatformController {

    private final ApplicationService applicationService;
    private final EventService eventService;
    private final UserService userService;

    @GetMapping("/organizer-applications")
    public ResponseEntity<List<AdminApplicationResponse>> getPendingApplications() {
        return ResponseEntity.ok(applicationService.getPendingApplications());
    }

    @PostMapping("/organizer-applications/{id}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable Long id) {
        applicationService.approveApplication(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/organizer-applications/{id}/deny")
    public ResponseEntity<Void> denyApplication(@PathVariable Long id) {
        applicationService.denyApplication(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AdminEventResponse>> getAllEvents(Pageable pageable) {
        return ResponseEntity.ok(eventService.getAllEventsForAdmin(pageable));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<Void> setUserRoles(@PathVariable Long userId, @RequestBody List<String> roleNames) {
        userService.setUserRoles(userId, roleNames);
        return ResponseEntity.ok().build();
    }
}