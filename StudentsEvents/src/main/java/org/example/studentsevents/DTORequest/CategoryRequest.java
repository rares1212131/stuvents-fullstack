package org.example.studentsevents.DTORequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be empty.")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters.")
    private String name;
}