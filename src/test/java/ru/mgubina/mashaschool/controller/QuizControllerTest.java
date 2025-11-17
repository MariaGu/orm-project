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
import ru.mgubina.mashaschool.dto.AnswerOptionCreateDto;
import ru.mgubina.mashaschool.dto.QuestionCreateDto;
import ru.mgubina.mashaschool.dto.QuizCreateDto;
import ru.mgubina.mashaschool.dto.TakeQuizDto;
import ru.mgubina.mashaschool.entity.*;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.repository.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class QuizControllerTest {

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
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Long moduleId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        quizSubmissionRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
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

        User student = userRepository.save(
                User.builder()
                        .name("Student")
                        .email("student@test.com")
                        .role(Role.STUDENT)
                        .build()
        );

        Module module = moduleRepository.save(
                Module.builder()
                        .title("Module")
                        .course(course)
                        .orderIndex(1)
                        .build()
        );
        moduleId = module.getId();

        studentId = student.getId();
    }

    @Test
    void testCreateQuiz() throws Exception {
        QuizCreateDto dto = QuizCreateDto.builder()
                .title("Test Quiz")
                .timeLimitSeconds(2000)
                .build();

        mockMvc.perform(post("/api/modules/" + moduleId + "/quiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.timeLimit").value(2000))
                .andExpect(jsonPath("$.moduleId").value(moduleId))
                .andExpect(jsonPath("$.moduleTitle").value("Module"));
    }

   @Test
    void testAddAnswerOption() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder()
                        .title("Test Quiz")
                        .module(moduleRepository.findById(moduleId).orElseThrow())
                        .build()
        );

        Question question = questionRepository.save(
                Question.builder()
                        .text("What is ORM?")
                        .quiz(quiz)
                        .build()
        );

        AnswerOptionCreateDto dto = AnswerOptionCreateDto.builder()
                .text("Object-Relational Mapping")
                .isCorrect(true)
                .build();

        mockMvc.perform(post("/api/questions/" + question.getId() + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("Object-Relational Mapping"))
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.questionText").value("What is ORM?"));
    }

    @Test
    void testTakeQuizWithInvalidQuestionId() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder()
                        .title("Test Quiz")
                        .module(moduleRepository.findById(moduleId).orElseThrow())
                        .build()
        );

        TakeQuizDto dto = TakeQuizDto.builder()
                .studentId(studentId)
                .answersByQuestion(Map.of(99999L, List.of(1L)))
                .build();

        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("does not belong to quiz")));
    }

    @Test
    void testTakeQuiz() throws Exception {
        Quiz quiz = quizRepository.save(
                Quiz.builder()
                        .title("Test Quiz")
                        .module(moduleRepository.findById(moduleId).orElseThrow())
                        .build()
        );

        Question question = questionRepository.save(
                Question.builder()
                        .text("What is ORM?")
                        .quiz(quiz)
                        .build()
        );

        AnswerOption correctOption = answerOptionRepository.save(
                AnswerOption.builder()
                        .text("Object-Relational Mapping")
                        .isCorrect(true)
                        .question(question)
                        .build()
        );

        TakeQuizDto dto = TakeQuizDto.builder()
                .studentId(studentId)
                .answersByQuestion(Map.of(question.getId(), List.of(correctOption.getId())))
                .build();

        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.studentName").value("Student"))
                .andExpect(jsonPath("$.quizId").value(quiz.getId()))
                .andExpect(jsonPath("$.score").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(1))
                .andExpect(jsonPath("$.takenAt").isNotEmpty());
    }
}

