package ru.mgubina.mashaschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mgubina.mashaschool.entity.CourseReview;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    List<CourseReview> findByCourseId(Long courseId);

    List<CourseReview> findByStudentId(Long studentId);
}

