package ru.mgubina.mashaschool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {

    private Long id;
    private String title;
    private Integer timeLimit;
    private Long moduleId;
    private String moduleTitle;
}

