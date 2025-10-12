package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TicketTypeRequest {
    private Long id;

    @NotBlank(message = "Ticket name cannot be empty.")
    private String name;

    @NotNull(message = "Price cannot be null.")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01.")
    private BigDecimal price;

    @NotNull(message = "Quantity cannot be null.")
    @Min(value = 1, message = "Total available tickets must be at least 1.")
    private Integer totalAvailable;
}
