package io.paideia.course.service.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import io.paideia.course.service.enums.CourseStatus;

public record CourseResponseDTO(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        CourseStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
