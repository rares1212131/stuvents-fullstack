package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "First name cannot be empty.")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty.")
    private String lastName;
}