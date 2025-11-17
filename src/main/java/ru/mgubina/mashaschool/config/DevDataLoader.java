package ru.mgubina.mashaschool.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.*;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.repository.*;


import java.time.LocalDate;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (courseRepository.count() > 0) {
            log.info("База данных уже содержит данные (курсов: {}), пропускаем загрузку",
                    courseRepository.count());
            return;
        }

        User teacher = User.builder()
                .name("Иван Гачин")
                .email("teacher@mashaschool.ru")
                .role(Role.TEACHER)
                .build();
        teacher = userRepository.save(teacher);

        User student1 = User.builder()
                .name("Анна Попова")
                .email("anna@student.ru")
                .role(Role.STUDENT)
                .build();
        student1 = userRepository.save(student1);

        User student2 = User.builder()
                .name("Петр Иванов")
                .email("petr@student.ru")
                .role(Role.STUDENT)
                .build();
        student2 = userRepository.save(student2);

        Category category = Category.builder()
                .name("Программирование")
                .build();
        category = categoryRepository.save(category);

        Tag tag1 = Tag.builder()
                .name("Java")
                .build();
        tag1 = tagRepository.save(tag1);

        Tag tag2 = Tag.builder()
                .name("ORM")
                .build();
        tag2 = tagRepository.save(tag2);
        log.info("Созданы теги: {}, {}", tag1.getName(), tag2.getName());

        Course course = Course.builder()
                .title("Hibernate и JPA")
                .description("Изучение Java")
                .duration("6 недель")
                .startDate(LocalDate.now().plusDays(7))
                .category(category)
                .teacher(teacher)
                .tags(Set.of(tag1, tag2))
                .build();
        course = courseRepository.save(course);

        Module module1 = Module.builder()
                .title("Введение в Python")
                .description("Основные концепции Python")
                .orderIndex(1)
                .course(course)
                .build();
        module1 = moduleRepository.save(module1);

        Module module2 = Module.builder()
                .title("Возможности Hibernate")
                .description("КHibernate для начинающий")
                .orderIndex(2)
                .course(course)
                .build();
        module2 = moduleRepository.save(module2);

        Lesson lesson1_1 = Lesson.builder()
                .title("HTML")
                .content("Основы HTML")
                .videoUrl("https://example.com/video1")
                .module(module1)
                .build();
        lesson1_1 = lessonRepository.save(lesson1_1);

        Lesson lesson1_2 = Lesson.builder()
                .title("Настройка Hibernate")
                .content("Конфигурация и первый проект с Hibernate")
                .videoUrl("https://example.com/video2")
                .module(module1)
                .build();
        lesson1_2 = lessonRepository.save(lesson1_2);

        Lesson lesson2_1 = Lesson.builder()
                .title("Ленивая загрузка")
                .content("Разбираем стратегии загрузки данных: LAZY vs EAGER")
                .videoUrl("https://example.com/video3")
                .module(module2)
                .build();
        lesson2_1 = lessonRepository.save(lesson2_1);

        Lesson lesson2_2 = Lesson.builder()
                .title("Кэширование в Hibernate")
                .content("Первый и второй уровень кэша")
                .module(module2)
                .build();
        lesson2_2 = lessonRepository.save(lesson2_2);

        Assignment assignment = Assignment.builder()
                .title("Практическое задание: настройка проекта")
                .description("Создайте простой проект с Hibernate и выполните базовые CRUD-операции")
                .dueDate(LocalDate.now().plusDays(14))
                .maxScore(100)
                .lesson(lesson1_2)
                .build();
        assignment = assignmentRepository.save(assignment);

        Quiz quiz = Quiz.builder()
                .title("Тест по введению в ORM")
                .timeLimit(30)
                .module(module1)
                .build();
        quiz = quizRepository.save(quiz);

        Question question1 = Question.builder()
                .text("Что такое ORM?")
                .type("SINGLE_CHOICE")
                .quiz(quiz)
                .build();
        question1 = questionRepository.save(question1);

        Question question2 = Question.builder()
                .text("Какая стратегия загрузки является ленивой?")
                .type("SINGLE_CHOICE")
                .quiz(quiz)
                .build();
        question2 = questionRepository.save(question2);

        AnswerOption option1_1 = AnswerOption.builder()
                .text("Object-Relational Mapping")
                .isCorrect(true)
                .question(question1)
                .build();
        answerOptionRepository.save(option1_1);

        AnswerOption option1_2 = AnswerOption.builder()
                .text("Object-Remote Method")
                .isCorrect(false)
                .question(question1)
                .build();
        answerOptionRepository.save(option1_2);

        AnswerOption option1_3 = AnswerOption.builder()
                .text("Operational Resource Manager")
                .isCorrect(false)
                .question(question1)
                .build();
        answerOptionRepository.save(option1_3);

        AnswerOption option2_1 = AnswerOption.builder()
                .text("EAGER")
                .isCorrect(false)
                .question(question2)
                .build();
        answerOptionRepository.save(option2_1);

        AnswerOption option2_2 = AnswerOption.builder()
                .text("LAZY")
                .isCorrect(true)
                .question(question2)
                .build();
        answerOptionRepository.save(option2_2);

        AnswerOption option2_3 = AnswerOption.builder()
                .text("IMMEDIATE")
                .isCorrect(false)
                .question(question2)
                .build();
        answerOptionRepository.save(option2_3);
    }
}

