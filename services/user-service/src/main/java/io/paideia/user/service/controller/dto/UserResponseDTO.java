package io.paideia.user.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID keycloakId,
        String displayName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}