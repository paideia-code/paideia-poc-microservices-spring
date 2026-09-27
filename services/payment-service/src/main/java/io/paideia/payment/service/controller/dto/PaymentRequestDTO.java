package io.paideia.payment.service.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDTO(
        @NotNull @DecimalMin("0.00") BigDecimal amount
) {}
