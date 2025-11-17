package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Course;
import ru.mgubina.mashaschool.entity.Enrollment;
import ru.mgubina.mashaschool.entity.User;
import ru.mgubina.mashaschool.exception.DuplicateEnrollmentException;
import ru.mgubina.mashaschool.repository.CourseRepository;
import ru.mgubina.mashaschool.repository.EnrollmentRepository;
import ru.mgubina.mashaschool.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public long enrollStudent(long courseId, long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + studentId));

        if (enrollmentRepository.findByUserIdAndCourseId(studentId, courseId).isPresent()) {
            throw new DuplicateEnrollmentException(
                    String.format("Student %d is already enrolled in course %d", studentId, courseId)
            );
        }

        Enrollment enrollment = Enrollment.builder()
                .user(student)
                .course(course)
                .enrollDate(LocalDate.now())
                .status("Active")
                .build();

        try {
            Enrollment saved = enrollmentRepository.save(enrollment);
            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEnrollmentException(
                    String.format("Student %d is already enrolled in course %d", studentId, courseId),
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesForStudent(long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new IllegalArgumentException("User not found: " + studentId);
        }

       return enrollmentRepository.findByUserId(studentId).stream()
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
                    // Инициализируем основные поля для использования вне транзакции
                    course.getId();
                    course.getTitle();
                    course.getDescription();
                    return course;
                })
                .toList();
    }

    @Transactional
    public boolean unenrollStudent(long courseId, long studentId) {
        return enrollmentRepository.findByUserIdAndCourseId(studentId, courseId)
                .map(enrollment -> {
                    enrollmentRepository.delete(enrollment);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<User> getStudentsForCourse(long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> {
                    User user = enrollment.getUser();
                    // Инициализируем основные поля для использования вне транзакции
                    user.getId();
                    user.getName();
                    user.getEmail();
                    return user;
                })
                .toList();
    }
}

