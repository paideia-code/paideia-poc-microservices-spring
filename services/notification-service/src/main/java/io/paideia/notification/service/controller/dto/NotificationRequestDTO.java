package io.paideia.notification.service.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequestDTO(
        @NotNull UUID recipientId,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String body
) {}
