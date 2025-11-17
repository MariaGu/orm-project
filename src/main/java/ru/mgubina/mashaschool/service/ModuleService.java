package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Lesson;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.repository.LessonRepository;
import ru.mgubina.mashaschool.repository.ModuleRepository;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public Long addLesson(Long moduleId, String title, String content, String videoUrl) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));

        Lesson lesson = Lesson.builder()
                .title(title)
                .content(content)
                .videoUrl(videoUrl)
                .module(module)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return savedLesson.getId();
    }

    @Transactional(readOnly = true)
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));
    }

    @Transactional
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Module not found: " + id);
        }
        moduleRepository.deleteById(id);
    }
}

