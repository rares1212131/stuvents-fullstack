package org.example.studentsevents.Controller;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTOResponse.EventResponse;
import org.example.studentsevents.Service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events") // This path is configured for public access
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // This endpoint remains for public search and filtering.
    @GetMapping
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String cityName,
            @PageableDefault(size = 9, sort = "eventDateTime") Pageable pageable) {

        // We now call the explicitly named "public" service method.
        Page<EventResponse> eventPage = eventService.searchPublicEvents(
                eventName, categoryName, cityName, pageable
        );
        return ResponseEntity.ok(eventPage);
    }

    // This endpoint remains for the public details page of an event.
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        // We now call the explicitly named "public" service method.
        EventResponse event = eventService.getPublicEventById(id);
        return ResponseEntity.ok(event);
    }
}