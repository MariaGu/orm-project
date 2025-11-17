package ru.mgubina.mashaschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mgubina.mashaschool.entity.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignmentId(Long assignmentId);

    List<Submission> findByStudentId(Long studentId);

    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
}

