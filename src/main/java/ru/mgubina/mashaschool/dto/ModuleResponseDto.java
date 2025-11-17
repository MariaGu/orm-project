package ru.mgubina.mashaschool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDto {

    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Long courseId;
    private String courseTitle;
}

