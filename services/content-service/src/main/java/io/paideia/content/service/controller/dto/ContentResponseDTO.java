package io.paideia.content.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ContentResponseDTO(
        UUID id,
        UUID courseId,
        String filename,
        String contentType,
        Long sizeBytes,
        String description,
        Map<String, Object> metadata,
        OffsetDateTime createdAt
) {}
