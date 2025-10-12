package org.example.studentsevents.DTOResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime eventDateTime;
    private String address;
    private String externalLink;
    private CategoryResponse category;
    private CityResponse city;
    private List<TicketTypeResponse> ticketTypes;
    private String eventImageUrl;
    private Double latitude;
    private Double longitude;
}