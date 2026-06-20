package io.paideia.content.service.service.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.paideia.content.service.controller.dto.ContentResponseDTO;
import io.paideia.content.service.model.entity.ContentEntity;
import io.paideia.content.service.service.ContentUploadCommand;

@Component
public class ContentMapper {

    public ContentEntity toEntity(ContentUploadCommand command) {
        UUID id = UUID.randomUUID();
        return ContentEntity.builder()
                .id(id)
                .courseId(command.courseId())
                .filename(command.filename())
                .contentType(command.contentType())
                .sizeBytes((long) command.fileBytes().length)
                .storageKey(storageKey(command.courseId(), id, command.filename()))
                .fileBytes(command.fileBytes())
                .description(command.description())
                .metadata(metadata(command.metadata()))
                .createdAt(Instant.now())
                .build();
    }

    public ContentResponseDTO toResponse(ContentEntity entity) {
        return new ContentResponseDTO(
                entity.getId(),
                entity.getCourseId(),
                entity.getFilename(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getDescription(),
                metadata(entity.getMetadata()),
                OffsetDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC)
        );
    }

    private Map<String, Object> metadata(Map<String, Object> metadata) {
        return metadata == null ? Map.of() : new LinkedHashMap<>(metadata);
    }

    private String storageKey(UUID courseId, UUID contentId, String filename) {
        return "courses/%s/contents/%s/%s".formatted(courseId, contentId, cleanFilename(filename));
    }

    private String cleanFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "content";
        }
        String normalized = filename.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }
}
