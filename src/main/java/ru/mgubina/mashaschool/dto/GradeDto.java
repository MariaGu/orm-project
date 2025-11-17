package ru.mgubina.mashaschool.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {

    @NotNull(message = "Score is required")
    @PositiveOrZero(message = "Score must be zero or positive")
    private Integer score;

    private String feedback;
}

