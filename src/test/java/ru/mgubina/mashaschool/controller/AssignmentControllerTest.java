package ru.mgubina.mashaschool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mgubina.mashaschool.dto.AssignmentCreateDto;
import ru.mgubina.mashaschool.dto.GradeDto;
import ru.mgubina.mashaschool.dto.SubmissionCreateDto;
import ru.mgubina.mashaschool.entity.*;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.repository.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AssignmentControllerTest {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private Long lessonId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        User teacher = userRepository.save(
                User.builder()
                        .name("Teacher")
                        .email("teacher@test.com")
                        .role(Role.TEACHER)
                        .build()
        );

        Category category = categoryRepository.save(
                Category.builder().name("Category").build()
        );

        Course course = courseRepository.save(
                Course.builder()
                        .title("Course")
                        .category(category)
                        .teacher(teacher)
                        .build()
        );

        Module module = moduleRepository.save(
                Module.builder()
                        .title("Module")
                        .course(course)
                        .orderIndex(1)
                        .build()
        );

        Lesson lesson = lessonRepository.save(
                Lesson.builder()
                        .title("Lesson")
                        .content("Content")
                        .module(module)
                        .build()
        );
        lessonId = lesson.getId();

        User student = userRepository.save(
                User.builder()
                        .name("Student")
                        .email("student@test.com")
                        .role(Role.STUDENT)
                        .build()
        );
        studentId = student.getId();
    }

    @Test
    void testCreateAssignment() throws Exception {
        AssignmentCreateDto dto = AssignmentCreateDto.builder()
                .title("Test Assignment")
                .description("Description")
                .maxScore(100)
                .build();

        mockMvc.perform(post("/api/lessons/" + lessonId + "/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Assignment"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.maxScore").value(100))
                .andExpect(jsonPath("$.lessonId").value(lessonId))
                .andExpect(jsonPath("$.lessonTitle").value("Lesson"));
    }


    @Test
    void testSubmitAssignment() throws Exception {
        SubmissionCreateDto dto = SubmissionCreateDto.builder()
                .studentId(studentId)
                .content("My content")
                .build();

        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .title("Test Assignment")
                        .maxScore(100)
                        .lesson(lessonRepository.findById(lessonId).orElseThrow())
                        .build()
        );

        mockMvc.perform(post("/api/assignments/" + assignment.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentName").value("Student"))
                .andExpect(jsonPath("$.assignmentId").value(assignment.getId()))
                .andExpect(jsonPath("$.assignmentTitle").value("Test Assignment"))
                .andExpect(jsonPath("$.content").value("My solution"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty())
                .andExpect(jsonPath("$.score").isEmpty())
                .andExpect(jsonPath("$.feedback").isEmpty());
    }

    @Test
    void testSubmitAssignmentDuplicate() throws Exception {

        SubmissionCreateDto dto = SubmissionCreateDto.builder()
                .studentId(studentId)
                .content("My content")
                .build();

        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .title("Test Assignment")
                        .maxScore(100)
                        .lesson(lessonRepository.findById(lessonId).orElseThrow())
                        .build()
        );

        mockMvc.perform(post("/api/assignments/" + assignment.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/assignments/" + assignment.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already submitted")));
    }

    @Test
    void testGradeSubmission() throws Exception {
        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .title("Test Assignment")
                        .maxScore(100)
                        .lesson(lessonRepository.findById(lessonId).orElseThrow())
                        .build()
        );

        Submission submission = submissionRepository.save(
                Submission.builder()
                        .assignment(assignment)
                        .student(userRepository.findById(studentId).orElseThrow())
                        .content("Content")
                        .build()
        );

        GradeDto dto = GradeDto.builder()
                .score(85)
                .feedback("Good work!")
                .build();

        mockMvc.perform(post("/api/submissions/" + submission.getId() + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGradeSubmissionExceedsMaxScore() throws Exception {
        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .title("Test Assignment")
                        .maxScore(100)
                        .lesson(lessonRepository.findById(lessonId).orElseThrow())
                        .build()
        );

        GradeDto dto = GradeDto.builder()
                .score(123)
                .feedback("Too high!")
                .build();

        Submission submission = submissionRepository.save(
                Submission.builder()
                        .assignment(assignment)
                        .student(userRepository.findById(studentId).orElseThrow())
                        .content("Content")
                        .build()
        );

        mockMvc.perform(post("/api/submissions/" + submission.getId() + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("exceeds max score")));
    }
}

