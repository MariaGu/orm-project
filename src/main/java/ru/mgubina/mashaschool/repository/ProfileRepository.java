package ru.mgubina.mashaschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mgubina.mashaschool.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}

