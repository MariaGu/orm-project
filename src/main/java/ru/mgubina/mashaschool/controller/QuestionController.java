package ru.mgubina.mashaschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.mashaschool.dto.AnswerOptionCreateDto;
import ru.mgubina.mashaschool.dto.AnswerOptionResponseDto;
import ru.mgubina.mashaschool.entity.AnswerOption;
import ru.mgubina.mashaschool.repository.AnswerOptionRepository;
import ru.mgubina.mashaschool.service.QuizService;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final AnswerOptionRepository answerOptionRepository;
    private final QuizService quizService;

    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AnswerOptionResponseDto addAnswerOption(@PathVariable Long id, @Valid @RequestBody AnswerOptionCreateDto dto) {
        Long optionId = quizService.addAnswerOption(id, dto.getText(), dto.getIsCorrect());

        AnswerOption option = answerOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Answer option not found"));

        return AnswerOptionResponseDto.builder()
                .id(option.getId())
                .text(option.getText())
                .isCorrect(option.getIsCorrect())
                .questionId(option.getQuestion().getId())
                .questionText(option.getQuestion().getText())
                .build();
    }
}

