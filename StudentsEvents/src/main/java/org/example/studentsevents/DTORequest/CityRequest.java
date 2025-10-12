package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CityRequest {

    @NotBlank(message = "City name cannot be empty.")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters.")
    private String name;
}