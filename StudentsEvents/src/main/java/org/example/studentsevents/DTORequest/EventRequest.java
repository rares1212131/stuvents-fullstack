package org.example.studentsevents.DTORequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventRequest {
    @NotBlank(message = "Event name cannot be empty")
    @Size(min = 3, max = 100, message = "Event name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Event date and time cannot be null")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDateTime;

    @NotBlank(message = "Address cannot be empty")
    private String address;

    private String externalLink;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "City ID cannot be null")
    private Long cityId;

    @NotEmpty(message = "Event must have at least one ticket type")
    private List<@Valid TicketTypeRequest> ticketTypes;
}