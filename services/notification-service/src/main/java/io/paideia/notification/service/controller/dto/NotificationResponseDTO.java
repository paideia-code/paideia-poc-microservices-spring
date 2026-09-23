package io.paideia.notification.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.paideia.notification.service.enums.NotificationStatus;

public record NotificationResponseDTO(
        UUID id,
        UUID studentId,
        String email,
        String type,
        String subject,
        String body,
        NotificationStatus status,
        OffsetDateTime createdAt
) {}
