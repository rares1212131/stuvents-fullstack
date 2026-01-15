package org.example.studentsevents.DTORequest;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "Ticket Type ID cannot be null.")
    private Long ticketTypeId;

    @NotNull(message = "Quantity cannot be null.")
    @Min(value = 1, message = "You must purchase at least 1 ticket.")
    private Integer quantity;
}