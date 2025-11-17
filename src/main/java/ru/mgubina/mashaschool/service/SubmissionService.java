package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Assignment;
import ru.mgubina.mashaschool.entity.Submission;
import ru.mgubina.mashaschool.entity.User;
import ru.mgubina.mashaschool.exception.DuplicateSubmissionException;
import ru.mgubina.mashaschool.repository.AssignmentRepository;
import ru.mgubina.mashaschool.repository.SubmissionRepository;
import ru.mgubina.mashaschool.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void grade(long submissionId, int score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        Integer maxScore = submission.getAssignment().getMaxScore();
        if (maxScore != null && score > maxScore) {
            throw new IllegalArgumentException(
                    String.format("Score %d exceeds maximum score %d for this assignment", score, maxScore)
            );
        }

        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        submission.setScore(score);
        submission.setFeedback(feedback);

        submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> getByStudent(long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new IllegalArgumentException("User not found: " + studentId);
        }

        return submissionRepository.findByStudentId(studentId).stream()
                .map(submission -> {
                    submission.getId();
                    submission.getContent();
                    submission.getScore();
                    submission.getSubmittedAt();
                    Assignment assignment = submission.getAssignment();
                    assignment.getId();
                    assignment.getTitle();
                    assignment.getMaxScore();
                    return submission;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Submission> getByAssignment(long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }

        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(submission -> {
                    submission.getId();
                    submission.getContent();
                    submission.getScore();
                    submission.getSubmittedAt();
                    User student = submission.getStudent();
                    student.getId();
                    student.getName();
                    student.getEmail();
                    return submission;
                })
                .toList();
    }

    @Transactional
    public long submit(long studentId, long assignmentId, String content) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + studentId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        if (submissionRepository.findByStudentIdAndAssignmentId(studentId, assignmentId).isPresent()) {
            throw new DuplicateSubmissionException(
                    String.format("Student %d has already submitted assignment %d", studentId, assignmentId)
            );
        }

        Submission submission = Submission.builder()
                .student(student)
                .assignment(assignment)
                .content(content)
                .submittedAt(OffsetDateTime.now())
                .build();

        try {
            Submission saved = submissionRepository.save(submission);
            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSubmissionException(
                    String.format("Student %d has already submitted assignment %d", studentId, assignmentId),
                    e
            );
        }
    }
}

