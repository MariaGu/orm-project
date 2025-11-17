package ru.mgubina.mashaschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.mashaschool.entity.Category;
import ru.mgubina.mashaschool.entity.Course;
import ru.mgubina.mashaschool.entity.Module;
import ru.mgubina.mashaschool.entity.User;
import ru.mgubina.mashaschool.repository.CategoryRepository;
import ru.mgubina.mashaschool.repository.CourseRepository;
import ru.mgubina.mashaschool.repository.ModuleRepository;
import ru.mgubina.mashaschool.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Course updateCourse(Long id, String title, String description, String duration, LocalDate startDate) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));

        if (title != null) {
            course.setTitle(title);
        }
        if (description != null) {
            course.setDescription(description);
        }
        if (duration != null) {
            course.setDuration(duration);
        }
        if (startDate != null) {
            course.setStartDate(startDate);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course createCourse(String title, String description, Long categoryId, Long teacherId,
                               String duration, LocalDate startDate) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        Course course = Course.builder()
                .title(title)
                .description(description)
                .category(category)
                .teacher(teacher)
                .duration(duration)
                .startDate(startDate)
                .build();

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Transactional
    public Long addModule(Long courseId, String title, String description, Integer orderIndex) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        Module module = Module.builder()
                .title(title)
                .description(description)
                .orderIndex(orderIndex)
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);
        return savedModule.getId();
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }

    @Transactional(readOnly = true)
    public Course getCourseWithContent(Long id) {
        return courseRepository.findById(id)
                .map(course -> {
                    course.getModules().size();
                    course.getModules().forEach(module -> module.getLessons().size());

                    course.getCategory().getName();
                    course.getTeacher().getName();
                    course.getTags().size();
                    course.getTags().forEach(tag -> tag.getName());

                    return course;
                })
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }
}

