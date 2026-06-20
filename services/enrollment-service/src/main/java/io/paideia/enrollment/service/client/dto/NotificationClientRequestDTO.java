package io.paideia.enrollment.service.client.dto;

import java.util.UUID;

public record NotificationClientRequestDTO(
        UUID recipientId,
        String type,
        String subject,
        String body
) {}
