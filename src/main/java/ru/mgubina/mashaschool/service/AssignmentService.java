package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Assignment;
import ru.mgubina.mashaschool.entity.Lesson;
import ru.mgubina.mashaschool.repository.AssignmentRepository;
import ru.mgubina.mashaschool.repository.LessonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public long createAssignment(long lessonId, String title, String description, Integer maxScore) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

        Assignment assignment = Assignment.builder()
                .title(title)
                .description(description)
                .maxScore(maxScore)
                .lesson(lesson)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return saved.getId();
    }

    @Transactional
    public void delete(long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Assignment not found: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Assignment getById(long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Assignment> getByLesson(long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found: " + lessonId);
        }

        return assignmentRepository.findByLessonId(lessonId);
    }
}

