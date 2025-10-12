package org.example.studentsevents.DTOResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerBookingResponse {
    private Long id;
    private LocalDateTime bookingDateTime;
    private PurchasedTicketResponse ticketType;
    private UserSummaryResponse user;
}