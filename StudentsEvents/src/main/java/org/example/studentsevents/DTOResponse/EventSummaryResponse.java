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
public class EventSummaryResponse {
    private Long id;
    private String name;
    private LocalDateTime eventDateTime;
}