package io.paideia.enrollment.service.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.paideia.enrollment.service.enums.EnrollmentStatus;
import io.paideia.enrollment.service.model.entity.EnrollmentEntity;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {

    boolean existsByCourseIdAndStudentIdAndStatus(UUID courseId, UUID studentId, EnrollmentStatus status);

    List<EnrollmentEntity> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);
}
