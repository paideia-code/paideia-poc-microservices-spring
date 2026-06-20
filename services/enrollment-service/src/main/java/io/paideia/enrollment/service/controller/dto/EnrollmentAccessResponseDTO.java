package io.paideia.enrollment.service.controller.dto;

import java.util.UUID;

public record EnrollmentAccessResponseDTO(
        UUID studentId,
        UUID courseId,
        boolean accessAllowed
) {}
