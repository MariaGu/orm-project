package ru.mgubina.mashaschool.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mgubina.mashaschool.entity.*;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.repository.*;
import ru.mgubina.mashaschool.exception.DuplicateSubmissionException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SubmissionServiceTest {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Очищаем данные перед каждым тестом
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void testSubmitDuplicateThrowsException() {
        Long assignmentId = createTestAssignment();
        User student = createTestStudent("Student", "student@test.com");

        submissionService.submit(student.getId(), assignmentId, "First submission");

        assertThatThrownBy(() -> submissionService.submit(student.getId(), assignmentId, "Second submission"))
                .isInstanceOf(DuplicateSubmissionException.class)
                .hasMessageContaining("already submitted");
    }
    @Test
    void testSubmitAssignmentSuccessfully() {
        Long assignmentId = createTestAssignment();
        User student = createTestStudent("Student", "student@test.com");

        long submissionId = submissionService.submit(student.getId(), assignmentId, "My solution");

        assertThat(submissionId).isPositive();

        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
        assertThat(submission.getContent()).isEqualTo("My solution");
        assertThat(submission.getSubmittedAt()).isNotNull();
        assertThat(submission.getScore()).isNull(); // Еще не оценено
    }

    @Test
    void testGradeNonExistentSubmissionThrowsException() {
        assertThatThrownBy(() -> submissionService.grade(99999L, 50, "Feedback"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Submission not found");
    }

   @Test
    void testCascadeDeleteOnAssignment() {
        Long assignmentId = createTestAssignment();
        User student1 = createTestStudent("Student 1", "student1@test.com");
        User student2 = createTestStudent("Student 2", "student2@test.com");

        long sub1 = submissionService.submit(student1.getId(), assignmentId, "Solution 1");
        long sub2 = submissionService.submit(student2.getId(), assignmentId, "Solution 2");

        assignmentService.delete(assignmentId);

        assertThat(assignmentRepository.existsById(assignmentId)).isFalse();
        assertThat(submissionRepository.existsById(sub2)).isFalse();
    }

    private Long createTestAssignmentWithMaxScore(String title, Integer maxScore) {
        Category category = categoryRepository.save(
                Category.builder().name("Test Category " + System.currentTimeMillis()).build()
        );

        User teacher = userRepository.save(
                User.builder()
                        .name("Teacher " + System.currentTimeMillis())
                        .email("teacher" + System.currentTimeMillis() + "@test.com")
                        .role(Role.TEACHER)
                        .build()
        );

        Course course = courseRepository.save(
                Course.builder()
                        .title("Test Course")
                        .description("Description")
                        .category(category)
                        .teacher(teacher)
                        .build()
        );

        Module module = moduleRepository.save(
                Module.builder()
                        .title("Test Module")
                        .course(course)
                        .orderIndex(1)
                        .build()
        );

        Lesson lesson = lessonRepository.save(
                Lesson.builder()
                        .title("Test Lesson")
                        .content("Content")
                        .module(module)
                        .build()
        );

        return assignmentService.createAssignment(lesson.getId(), title, "Description", maxScore);
    }

    private Long createTestAssignment() {
        return createTestAssignment("Test Assignment");
    }

    private Long createTestAssignment(String title) {
        return createTestAssignmentWithMaxScore(title, 100);
    }

    private User createTestStudent(String name, String email) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .email(email)
                        .role(Role.STUDENT)
                        .build()
        );
    }
}

