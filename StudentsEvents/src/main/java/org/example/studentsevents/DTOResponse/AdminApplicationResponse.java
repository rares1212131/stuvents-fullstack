

package org.example.studentsevents.DTOResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.studentsevents.model.ApplicationStatus;
@Getter
@Setter
@AllArgsConstructor
public class AdminApplicationResponse {
    private Long id;
    private ApplicationStatus status;
    private String reason;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
}