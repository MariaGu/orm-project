package ru.mgubina.mashaschool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDto {

    private Long id;
    private String title;
    private String description;
    private Integer maxScore;
    private Long lessonId;
    private String lessonTitle;
}

