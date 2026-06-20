package io.paideia.enrollment.service.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequestDTO(
        @NotNull UUID courseId
) {}
