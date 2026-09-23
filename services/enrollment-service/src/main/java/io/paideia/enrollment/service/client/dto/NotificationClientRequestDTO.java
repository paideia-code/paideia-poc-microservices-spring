package io.paideia.enrollment.service.client.dto;

public record NotificationClientRequestDTO(
        String type,
        String subject,
        String body) {
}