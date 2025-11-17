package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.AssignmentCreateDto;
import ru.mgubina.mashaschool.dto.AssignmentResponseDto;
import ru.mgubina.mashaschool.entity.Assignment;
import ru.mgubina.mashaschool.repository.AssignmentRepository;
import ru.mgubina.mashaschool.service.AssignmentService;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;

    @PostMapping("/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AssignmentResponseDto createAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentCreateDto dto) {
        Long assignmentId = assignmentService.createAssignment(
                id,
                dto.getTitle(),
                dto.getDescription(),
                dto.getMaxScore()
        );

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        return AssignmentResponseDto.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .maxScore(assignment.getMaxScore())
                .lessonId(assignment.getLesson().getId())
                .lessonTitle(assignment.getLesson().getTitle())
                .build();
    }
}

