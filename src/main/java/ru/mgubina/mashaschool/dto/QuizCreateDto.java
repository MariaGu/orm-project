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
public class QuizCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    @Positive(message = "Time limit must be positive if provided")
    private Integer timeLimitSeconds;
}

