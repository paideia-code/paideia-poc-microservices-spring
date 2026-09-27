package io.paideia.content.service.service;

import java.util.Map;
import java.util.UUID;

public record ContentUploadCommand(
        UUID courseId,
        String filename,
        String contentType,
        byte[] fileBytes,
        String description,
        Map<String, Object> metadata
) {}
