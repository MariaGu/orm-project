package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.SubmissionCreateDto;
import ru.mgubina.mashaschool.dto.SubmissionResponseDto;
import ru.mgubina.mashaschool.entity.Submission;
import ru.mgubina.mashaschool.repository.SubmissionRepository;
import ru.mgubina.mashaschool.service.SubmissionService;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public SubmissionResponseDto submitAssignment(@PathVariable Long id, @Valid @RequestBody SubmissionCreateDto dto) {
        Long submissionId = submissionService.submit(
                dto.getStudentId(),
                id,
                dto.getContent()
        );

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .content(submission.getContent())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .build();
    }
}

