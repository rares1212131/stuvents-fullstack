package org.example.studentsevents.DTOResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrganizerEventResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime eventDateTime;
    private String address;
    private CategoryResponse category;
    private CityResponse city;
    private List<OrganizerTicketTypeResponse> ticketTypes;
    private String eventImageUrl;
    private Double latitude;
    private Double longitude;
}