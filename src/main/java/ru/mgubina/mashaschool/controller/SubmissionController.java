package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.GradeDto;
import ru.mgubina.mashaschool.service.SubmissionService;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/{id}/grade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void gradeSubmission(@PathVariable Long id, @Valid @RequestBody GradeDto dto) {
        submissionService.grade(id, dto.getScore(), dto.getFeedback());
    }
}

