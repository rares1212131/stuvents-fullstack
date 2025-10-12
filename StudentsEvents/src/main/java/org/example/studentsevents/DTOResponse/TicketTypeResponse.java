package org.example.studentsevents.DTOResponse;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.studentsevents.model.AvailabilityStatus; // You will need to create this enum
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private AvailabilityStatus availability;
    private Integer maxPurchaseQuantity;
}