package io.paideia.content.service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.paideia.content.service.client.EnrollmentAccessClient;
import io.paideia.content.service.controller.dto.ContentResponseDTO;
import io.paideia.content.service.exception.custom.ContentAccessDeniedException;
import io.paideia.content.service.exception.custom.InvalidContentUploadException;
import io.paideia.content.service.model.repository.ContentRepository;
import io.paideia.content.service.service.mapper.ContentMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final EnrollmentAccessClient enrollmentAccessClient;

    public ContentResponseDTO create(ContentUploadCommand command) {
        validateUpload(command);
        var saved = contentRepository.save(contentMapper.toEntity(command));
        return contentMapper.toResponse(saved);
    }

    private void validateUpload(ContentUploadCommand command) {
        if (command.fileBytes() == null || command.fileBytes().length == 0) {
            throw new InvalidContentUploadException("Content file is required");
        }
        if (command.fileBytes().length > MAX_FILE_SIZE_BYTES) {
            throw new InvalidContentUploadException("Content file exceeds 5 MB limit");
        }
    }

    public List<ContentResponseDTO> findPurchasedCourseContents(UUID courseId, UUID studentId) {
        if (!enrollmentAccessClient.canAccessContent(studentId, courseId)) {
            throw new ContentAccessDeniedException(studentId, courseId);
        }
        return contentRepository.findByCourseId(courseId).stream()
                .map(contentMapper::toResponse)
                .toList();
    }
}
