package ru.mgubina.mashaschool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionResponseDto {

    private Long id;
    private String text;
    private Boolean isCorrect;
    private Long questionId;
    private String questionText;
}

