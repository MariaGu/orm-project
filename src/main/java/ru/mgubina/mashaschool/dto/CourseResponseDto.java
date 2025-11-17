package ru.mgubina.mashaschool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {

    private Long id;
    private String title;
    private String description;
    private String duration;
    private LocalDate startDate;
    private String categoryName;
    private String teacherName;
    private List<String> tagNames;
}

