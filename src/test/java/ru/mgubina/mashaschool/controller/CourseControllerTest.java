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
import ru.mgubina.mashaschool.dto.CourseCreateDto;
import ru.mgubina.mashaschool.dto.ModuleCreateDto;
import ru.mgubina.mashaschool.entity.*;
import ru.mgubina.mashaschool.entity.Category;
import ru.mgubina.mashaschool.entity.Role;
import ru.mgubina.mashaschool.entity.Tag;
import ru.mgubina.mashaschool.entity.User;
import ru.mgubina.mashaschool.repository.*;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CourseControllerTest {

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
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Long teacherId;
    private Long categoryId;
    private Long tagId;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(
                Category.builder().name("Test Category").build()
        );
        categoryId = category.getId();

        User teacher = userRepository.save(
                User.builder()
                        .name("Test Teacher")
                        .email("teacher@test.com")
                        .role(Role.TEACHER)
                        .build()
        );
        teacherId = teacher.getId();

        Tag tag = tagRepository.save(
                Tag.builder().name("Test Tag").build()
        );
        tagId = tag.getId();
    }

    @Test
    void testCreateCourse() throws Exception {
        CourseCreateDto dto = CourseCreateDto.builder()
                .title("Test Course")
                .description("Test Description")
                .categoryId(categoryId)
                .teacherId(teacherId)
                .tagIds(Set.of(tagId))
                .build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Course"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.categoryName").value("Test Category"))
                .andExpect(jsonPath("$.teacherName").value("Test Teacher"))
                .andExpect(jsonPath("$.tagNames").isArray())
                .andExpect(jsonPath("$.tagNames[0]").value("Test Tag"));
    }

    @Test
    void testUnenrollStudent() throws Exception {
        User teacher = userRepository.findById(teacherId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();

        var course = courseRepository.save(
                Course.builder()
                        .title("Test Course")
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

        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .param("userId", student.getId().toString()))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/courses/" + course.getId() + "/enroll")
                        .param("userId", student.getId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateCourseValidationFails() throws Exception {
        CourseCreateDto dto = CourseCreateDto.builder()
                .title("")
                .categoryId(categoryId)
                .teacherId(teacherId)
                .build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testGetCourseNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Course not found")));
    }

    @Test
    void testAddModule() throws Exception {
        User teacher = userRepository.findById(teacherId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();

        var course = courseRepository.save(
                Course.builder()
                        .title("Test Course")
                        .category(category)
                        .teacher(teacher)
                        .build()
        );

        ModuleCreateDto dto = ModuleCreateDto.builder()
                .title("Test Module")
                .description("Test Description")
                .orderIndex(1)
                .build();

        mockMvc.perform(post("/api/courses/" + course.getId() + "/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.courseTitle").value("Test Course"));
    }

    @Test
    void testEnrollStudent() throws Exception {
        User teacher = userRepository.findById(teacherId).orElseThrow();

        Category category = categoryRepository.findById(categoryId).orElseThrow();

        var course = courseRepository.save(
                Course.builder()
                        .title("Test Course")
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

        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .param("userId", student.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.studentName").value("Student"))
                .andExpect(jsonPath("$.courseId").value(course.getId()))
                .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void testEnrollStudentDuplicate() throws Exception {
        User teacher = userRepository.findById(teacherId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();


        Course course = courseRepository.save(
                Course.builder()
                        .title("Test Course")
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

        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .param("userId", student.getId().toString()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .param("userId", student.getId().toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already enrolled")));
    }


}

