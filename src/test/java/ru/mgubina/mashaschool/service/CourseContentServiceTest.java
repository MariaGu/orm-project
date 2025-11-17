package ru.mgubina.mashaschool.service;

import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CourseContentServiceTest {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CourseService courseService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Long teacherId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        User teacher = User.builder()
                .name("Test Teacher")
                .email("teacher@test.com")
                .role(Role.TEACHER)
                .build();
        teacher = userRepository.save(teacher);
        teacherId = teacher.getId();

        Category category = Category.builder()
                .name("Test Category")
                .build();
        category = categoryRepository.save(category);
        categoryId = category.getId();
    }

    @Test
    void testCreateAndReadCourseWithContent() {
        Course createdCourse = courseService.createCourse(
                "Test Course",
                "Test Description",
                categoryId,
                teacherId,
                "4 weeks",
                null
        );
        Long courseId = createdCourse.getId();

        Long module1Id = courseService.addModule(courseId, "Module 1", "Description 1", 1);
        Long module2Id = courseService.addModule(courseId, "Module 2", "Description 2", 2);

        moduleService.addLesson(module1Id, "Lesson 1.1", "Content 1.1", null);
        moduleService.addLesson(module1Id, "Lesson 1.2", "Content 1.2", null);
        moduleService.addLesson(module2Id, "Lesson 2.1", "Content 2.1", null);

        Course course = courseService.getCourseWithContent(courseId);

        assertThat(course).isNotNull();
        assertThat(course.getTitle()).isEqualTo("Test Course");
        assertThat(course.getModules()).hasSize(2);

        Module firstModule = course.getModules().stream()
                .filter(m -> m.getTitle().equals("Module 1"))
                .findFirst()
                .orElseThrow();
        assertThat(firstModule.getLessons()).hasSize(2);

        Module secondModule = course.getModules().stream()
                .filter(m -> m.getTitle().equals("Module 2"))
                .findFirst()
                .orElseThrow();
        assertThat(secondModule.getLessons()).hasSize(1);
    }

    @Test
    void testCascadeOnPartialDelete() {
        Course course = courseService.createCourse(
                "Test Course",
                "Test Description",
                categoryId,
                teacherId,
                "4 weeks",
                null
        );
        Long courseId = course.getId();

        Long module1Id = courseService.addModule(courseId, "Module 1", "First", 1);
        Long lesson1Id = moduleService.addLesson(module1Id, "Lesson 1", "Content 1", null);
        Long lesson2Id = moduleService.addLesson(module1Id, "Lesson 2", "Content 2", null);

        Assignment assignment1 = Assignment.builder()
                .title("Assignment 1")
                .description("Test")
                .lesson(lessonRepository.findById(lesson1Id).orElseThrow())
                .maxScore(100)
                .build();
        assignmentRepository.save(assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Assignment 2")
                .description("Test")
                .lesson(lessonRepository.findById(lesson2Id).orElseThrow())
                .maxScore(100)
                .build();
        assignmentRepository.save(assignment2);

        lessonService.deleteLesson(lesson1Id);

        assertThat(lessonRepository.existsById(lesson1Id)).isFalse();
        assertThat(lessonRepository.existsById(lesson2Id)).isTrue();
        assertThat(moduleRepository.existsById(module1Id)).isTrue();

        long assignmentCount = assignmentRepository.count();
        assertThat(assignmentCount).isEqualTo(1);
    }
}

