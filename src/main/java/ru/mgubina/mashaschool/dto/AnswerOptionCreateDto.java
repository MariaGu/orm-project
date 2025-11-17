package ru.mgubina.mashaschool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionCreateDto {

    @NotBlank(message = "Text is required")
    private String text;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
}

