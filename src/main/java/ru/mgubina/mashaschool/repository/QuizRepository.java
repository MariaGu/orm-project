package ru.mgubina.mashaschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mgubina.mashaschool.entity.Quiz;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByModuleId(Long moduleId);
}

