package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "New password must be at least 8 characters long.")
    private String newPassword;
}