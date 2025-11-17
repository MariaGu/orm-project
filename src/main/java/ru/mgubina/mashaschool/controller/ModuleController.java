package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.LessonCreateDto;
import ru.mgubina.mashaschool.dto.LessonResponseDto;
import ru.mgubina.mashaschool.dto.QuizCreateDto;
import ru.mgubina.mashaschool.dto.QuizResponseDto;
import ru.mgubina.mashaschool.entity.Lesson;
import ru.mgubina.mashaschool.entity.Quiz;
import ru.mgubina.mashaschool.repository.LessonRepository;
import ru.mgubina.mashaschool.repository.QuizRepository;
import ru.mgubina.mashaschool.service.ModuleService;
import ru.mgubina.mashaschool.service.QuizService;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final QuizService quizService;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;

    @PostMapping("/{id}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public QuizResponseDto createQuiz(@PathVariable Long id, @Valid @RequestBody QuizCreateDto dto) {
        Long quizId = quizService.createQuiz(
                id,
                dto.getTitle(),
                dto.getTimeLimitSeconds()
        );

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        return QuizResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .timeLimit(quiz.getTimeLimit())
                .moduleId(quiz.getModule().getId())
                .moduleTitle(quiz.getModule().getTitle())
                .build();
    }

    @PostMapping("/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public LessonResponseDto addLesson(@PathVariable Long id, @Valid @RequestBody LessonCreateDto dto) {
        Long lessonId = moduleService.addLesson(
                id,
                dto.getTitle(),
                dto.getContent(),
                dto.getVideoUrl()
        );

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        return LessonResponseDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .moduleId(lesson.getModule().getId())
                .moduleTitle(lesson.getModule().getTitle())
                .build();
    }
}

