package io.paideia.enrollment.service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentClientResponseDTO(
        UUID id,
        BigDecimal amount,
        String status
) {}
