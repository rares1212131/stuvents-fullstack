package org.example.studentsevents.DTOResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerTicketTypeResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer totalAvailable;
    private long ticketsSold;
}