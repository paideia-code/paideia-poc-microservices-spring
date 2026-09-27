package io.paideia.notification.service.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationRequestDTO(
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String body) {
}
