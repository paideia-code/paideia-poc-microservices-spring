package io.paideia.user.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.paideia.user.service.enums.UserRole;

public record UserResponseDTO(
        UUID id,
        String email,
        String name,
        UserRole role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
