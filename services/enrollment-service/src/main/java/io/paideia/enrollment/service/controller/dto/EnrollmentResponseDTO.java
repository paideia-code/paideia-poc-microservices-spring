package io.paideia.enrollment.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.paideia.enrollment.service.enums.EnrollmentStatus;

public record EnrollmentResponseDTO(
        UUID id,
        UUID courseId,
        UUID studentId,
        EnrollmentStatus status,
        UUID paymentId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
