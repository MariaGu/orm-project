package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.QuestionCreateDto;
import ru.mgubina.mashaschool.dto.QuestionResponseDto;
import ru.mgubina.mashaschool.dto.QuizSubmissionResponseDto;
import ru.mgubina.mashaschool.dto.TakeQuizDto;
import ru.mgubina.mashaschool.entity.Question;
import ru.mgubina.mashaschool.entity.Quiz;
import ru.mgubina.mashaschool.entity.QuizSubmission;
import ru.mgubina.mashaschool.repository.QuestionRepository;
import ru.mgubina.mashaschool.service.QuizService;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuestionRepository questionRepository;

    private final QuizService quizService;

    @PostMapping("/{id}/take")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public QuizSubmissionResponseDto takeQuiz(@PathVariable Long id, @Valid @RequestBody TakeQuizDto dto) {
        QuizSubmission submission = quizService.takeQuiz(
                dto.getStudentId(),
                id,
                dto.getAnswersByQuestion()
        );

        Quiz quiz = submission.getQuiz();
        int totalQuestions = quiz.getQuestions().size();

        return QuizSubmissionResponseDto.builder()
                .id(submission.getId())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .score(submission.getScore())
                .totalQuestions(totalQuestions)
                .takenAt(submission.getTakenAt())
                .build();
    }

    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public QuestionResponseDto addQuestion(@PathVariable Long id, @Valid @RequestBody QuestionCreateDto dto) {
        Long questionId = quizService.addQuestion(id, dto.getText());

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        return QuestionResponseDto.builder()
                .id(question.getId())
                .text(question.getText())
                .quizId(question.getQuiz().getId())
                .quizTitle(question.getQuiz().getTitle())
                .build();
    }
 }

