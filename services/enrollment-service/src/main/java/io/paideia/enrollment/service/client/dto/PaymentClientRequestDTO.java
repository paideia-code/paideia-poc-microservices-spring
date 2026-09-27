package io.paideia.enrollment.service.client.dto;

import java.math.BigDecimal;

public record PaymentClientRequestDTO(
        BigDecimal amount
) {}
