package org.example.studentsevents.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.BookingRequest;
import org.example.studentsevents.DTOResponse.BookingResponse;
import org.example.studentsevents.Service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')") // Ensures only logged-in users can book
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @PageableDefault(size = 10, sort = "bookingDateTime") Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getMyBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> getMyBookingById(@PathVariable Long bookingId) {
        BookingResponse booking = bookingService.getMyBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }
}