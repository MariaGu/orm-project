package ru.mgubina.mashaschool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Positive(message = "Max score must be positive if provided")
    private Integer maxScore;
}

