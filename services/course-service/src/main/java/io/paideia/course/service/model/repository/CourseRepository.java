package io.paideia.course.service.model.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.paideia.course.service.model.entity.CourseEntity;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    boolean existsByTitle(String title);
}
