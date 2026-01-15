

package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerApplicationRequest {

    @NotBlank(message = "Reason cannot be empty.")
    @Size(min = 50, max = 2000, message = "Reason must be between 50 and 2000 characters.")
    private String reason;
}