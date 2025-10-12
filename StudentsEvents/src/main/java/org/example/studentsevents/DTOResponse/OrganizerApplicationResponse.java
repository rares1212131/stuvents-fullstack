// CREATE NEW FILE: src/main/java/org/example/studentsevents/DTOResponse/OrganizerApplicationResponse.java

package org.example.studentsevents.DTOResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.studentsevents.model.ApplicationStatus;

/**
 * DTO for showing a user the status of their own organizer application.
 */
@Getter
@Setter
@AllArgsConstructor
public class OrganizerApplicationResponse {
    private Long id;
    private ApplicationStatus status;
    private String reason;
}