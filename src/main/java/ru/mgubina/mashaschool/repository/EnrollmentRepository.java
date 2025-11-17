package ru.mgubina.mashaschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mgubina.mashaschool.entity.Enrollment;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByUserId(Long userId);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
}

