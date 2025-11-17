package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Lesson;
import ru.mgubina.mashaschool.repository.LessonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByModuleId(Long moduleId) {
        return lessonRepository.findByModuleId(moduleId);
    }

    @Transactional
    public Lesson updateLesson(Long id, String title, String content, String videoUrl) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));

        if (title != null) {
            lesson.setTitle(title);
        }
        if (content != null) {
            lesson.setContent(content);
        }
        if (videoUrl != null) {
            lesson.setVideoUrl(videoUrl);
        }

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new IllegalArgumentException("Lesson not found: " + id);
        }
        lessonRepository.deleteById(id);
    }
}

