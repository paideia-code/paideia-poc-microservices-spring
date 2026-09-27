package io.paideia.payment.service.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import io.paideia.payment.service.enums.PaymentStatus;

public record PaymentResponseDTO(
        UUID id,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
