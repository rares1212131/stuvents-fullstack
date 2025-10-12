package org.example.studentsevents.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.EventRequest;
import org.example.studentsevents.DTORequest.TicketTypeRequest;
import org.example.studentsevents.DTOResponse.OrganizerBookingResponse;
import org.example.studentsevents.DTOResponse.OrganizerEventResponse;
import org.example.studentsevents.DTOResponse.OrganizerTicketTypeResponse;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.Service.EventService;
import org.example.studentsevents.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
public class OrganizerEventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrganizerEventResponse> createEvent(
            @Valid @RequestPart("event") EventRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        OrganizerEventResponse newEvent = eventService.createEventForOrganizer(request, imageFile);
        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    @GetMapping("/my-events")
    public ResponseEntity<Page<OrganizerEventResponse>> getMyEvents(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<OrganizerEventResponse> events = eventService.getEventsForOrganizer(currentUser, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizerEventResponse> getEventById(@PathVariable Long id) {
        OrganizerEventResponse event = eventService.getEventForOrganizerById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizerEventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestPart("event") EventRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        OrganizerEventResponse updatedEvent = eventService.updateEventForOrganizer(id, request, imageFile);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEventForOrganizer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/bookings")
    public ResponseEntity<Page<OrganizerBookingResponse>> getEventBookings(@PathVariable Long eventId, Pageable pageable) {
        Page<OrganizerBookingResponse> bookings = eventService.getBookingsForEvent(eventId, pageable);
        return ResponseEntity.ok(bookings);
    }
    @PostMapping("/{eventId}/ticket-types")
    public ResponseEntity<OrganizerTicketTypeResponse> addTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeRequest ticketTypeRequest) {
        OrganizerTicketTypeResponse newTicketType = eventService.addTicketTypeToEvent(eventId, ticketTypeRequest);
        return new ResponseEntity<>(newTicketType, HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<OrganizerTicketTypeResponse> updateTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId,
            @Valid @RequestBody TicketTypeRequest ticketTypeRequest) {
        OrganizerTicketTypeResponse updatedTicketType = eventService.updateTicketType(eventId, ticketTypeId, ticketTypeRequest);
        return ResponseEntity.ok(updatedTicketType);
    }

    @DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<Void> deleteTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId) {
        eventService.deleteTicketType(eventId, ticketTypeId);
        return ResponseEntity.noContent().build();
    }

    // You would continue to add ticket type management endpoints here...

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}