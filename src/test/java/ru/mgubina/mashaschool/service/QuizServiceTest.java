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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class QuizServiceTest {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private QuizService quizService;

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
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    }

    @Test
    void testTakeQuizWithInvalidOptionId() {
        QuizStructure structure = createQuizWithTwoQuestions();
        User student = createTestStudent("Student", "student@test.com");

        Map<Long, List<Long>> answers = Map.of(
                structure.question1Id, List.of(99999L)  // Несуществующий optionId
        );

        assertThatThrownBy(() -> quizService.takeQuiz(student.getId(), structure.quizId, answers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to question");
    }

    @Test
    void testCreateQuizAndStructure() {
        Long moduleId = createTestModule();

        long quizId = quizService.createQuiz(moduleId, "Test Quiz", 1800);

        Quiz quiz = quizService.getQuizById(quizId);
        assertThat(quiz).isNotNull();
        assertThat(quiz.getTitle()).isEqualTo("Test Quiz");
        assertThat(quiz.getTimeLimit()).isEqualTo(1800);

        long question1Id = quizService.addQuestion(quizId, "What is ORM?");
        long question2Id = quizService.addQuestion(quizId, "What is LAZY loading?");

        Question q1 = questionRepository.findById(question1Id).orElseThrow();
        Question q2 = questionRepository.findById(question2Id).orElseThrow();
        assertThat(q1.getText()).isEqualTo("What is ORM?");
        assertThat(q2.getText()).isEqualTo("What is LAZY loading?");

        long option1_1 = quizService.addAnswerOption(question1Id, "Object-Relational Mapping", true);
        long option1_2 = quizService.addAnswerOption(question1Id, "Object-Remote Method", false);
        long option1_3 = quizService.addAnswerOption(question1Id, "Operational Resource", false);

        long option2_1 = quizService.addAnswerOption(question2Id, "Loads data on demand", true);
        long option2_2 = quizService.addAnswerOption(question2Id, "Loads data immediately", false);
        long option2_3 = quizService.addAnswerOption(question2Id, "Reduces memory usage", true);

        List<AnswerOption> q1Options = answerOptionRepository.findByQuestionId(question1Id);
        List<AnswerOption> q2Options = answerOptionRepository.findByQuestionId(question2Id);

        assertThat(q1Options).hasSize(3);
        assertThat(q1Options.stream().filter(AnswerOption::getIsCorrect).count()).isEqualTo(1);

        assertThat(q2Options).hasSize(3);
        assertThat(q2Options.stream().filter(AnswerOption::getIsCorrect).count()).isEqualTo(2);
    }

    private Long createTestModule() {
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

        return module.getId();
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

    private record QuizStructure(
            long quizId,
            long question1Id, long option1_1, long option1_2, long option1_3,
            long question2Id, long option2_1, long option2_2, long option2_3
    ) {
    }

    private QuizStructure createQuizWithTwoQuestions() {
        Long moduleId = createTestModule();

        long quizId = quizService.createQuiz(moduleId, "Test Quiz", 1800);

        long question1Id = quizService.addQuestion(quizId, "What is ORM?");
        long option1_1 = quizService.addAnswerOption(question1Id, "Object-Relational Mapping", true);
        long option1_2 = quizService.addAnswerOption(question1Id, "Object-Remote Method", false);
        long option1_3 = quizService.addAnswerOption(question1Id, "Operational Resource", false);

        long question2Id = quizService.addQuestion(quizId, "What is LAZY loading?");
        long option2_1 = quizService.addAnswerOption(question2Id, "Loads data on demand", true);
        long option2_2 = quizService.addAnswerOption(question2Id, "Loads data immediately", false);
        long option2_3 = quizService.addAnswerOption(question2Id, "Reduces memory usage", true);

        return new QuizStructure(
                quizId,
                question1Id, option1_1, option1_2, option1_3,
                question2Id, option2_1, option2_2, option2_3
        );
    }
}

