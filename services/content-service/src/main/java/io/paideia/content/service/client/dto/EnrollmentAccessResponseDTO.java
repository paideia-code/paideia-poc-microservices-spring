package io.paideia.content.service.client.dto;

import java.util.UUID;

public record EnrollmentAccessResponseDTO(
        UUID studentId,
        UUID courseId,
        boolean accessAllowed
) {}
