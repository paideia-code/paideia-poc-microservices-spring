package io.paideia.content.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.paideia.content.service.client.EnrollmentAccessClient;
import io.paideia.content.service.exception.custom.ContentAccessDeniedException;
import io.paideia.content.service.exception.custom.InvalidContentUploadException;
import io.paideia.content.service.model.entity.ContentEntity;
import io.paideia.content.service.model.repository.ContentRepository;
import io.paideia.content.service.service.mapper.ContentMapper;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private EnrollmentAccessClient enrollmentAccessClient;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentService(
                contentRepository,
                new ContentMapper(),
                enrollmentAccessClient);
    }

    @Test
    void createPersistsUploadedCourseContent() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000001");
        byte[] fileBytes = "PDF bytes".getBytes(StandardCharsets.UTF_8);
        var command = new ContentUploadCommand(
                courseId,
                "intro.pdf",
                "application/pdf",
                fileBytes,
                "Intro",
                Map.<String, Object>of(
                        "lessonNumber", 1,
                        "pages", 12,
                        "kind", "syllabus"));

        when(contentRepository.save(any(ContentEntity.class))).thenAnswer(invocation -> {
            ContentEntity entity = invocation.getArgument(0);
            assertThat(entity.getStorageKey())
                    .isEqualTo("courses/%s/contents/%s/intro.pdf".formatted(courseId, entity.getId()));
            entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return entity;
        });

        var response = contentService.create(command);

        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.filename()).isEqualTo("intro.pdf");
        assertThat(response.sizeBytes()).isEqualTo((long) fileBytes.length);
        assertThat(response.metadata())
                .containsEntry("lessonNumber", 1)
                .containsEntry("pages", 12)
                .containsEntry("kind", "syllabus");
        assertThat(response.createdAt().toInstant()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(response.createdAt().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void createRejectsEmptyFile() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000001");
        var command = new ContentUploadCommand(
                courseId,
                "empty.pdf",
                "application/pdf",
                new byte[0],
                "Empty",
                Map.of());

        assertThatThrownBy(() -> contentService.create(command))
                .isInstanceOf(InvalidContentUploadException.class)
                .hasMessage("Content file is required");
    }

    @Test
    void createRejectsFileOverFiveMb() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000001");
        var command = new ContentUploadCommand(
                courseId,
                "big.pdf",
                "application/pdf",
                new byte[(5 * 1024 * 1024) + 1],
                "Big",
                Map.of());

        assertThatThrownBy(() -> contentService.create(command))
                .isInstanceOf(InvalidContentUploadException.class)
                .hasMessage("Content file exceeds 5 MB limit");
    }

    @Test
    void findPurchasedCourseContentsReturnsContentsWhenStudentIsEnrolled() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000001");
        var content = ContentEntity.builder()
                .id(UUID.fromString("22222222-0000-0000-0000-000000000001"))
                .courseId(courseId)
                .filename("intro.pdf")
                .contentType("application/pdf")
                .sizeBytes(100L)
                .storageKey("courses/%s/contents/22222222-0000-0000-0000-000000000001/intro.pdf".formatted(courseId))
                .description("Intro")
                .metadata(Map.<String, Object>of("pages", 12))
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(enrollmentAccessClient.canAccessContent(courseId)).thenReturn(true);
        when(contentRepository.findByCourseId(courseId)).thenReturn(List.of(content));

        var response = contentService.findPurchasedCourseContents(courseId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().filename()).isEqualTo("intro.pdf");
        assertThat(response.getFirst().metadata()).containsEntry("pages", 12);
    }

    @Test
    void findPurchasedCourseContentsRejectsStudentWithoutEnrollment() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000001");

        when(enrollmentAccessClient.canAccessContent(courseId)).thenReturn(false);

        assertThatThrownBy(() -> contentService.findPurchasedCourseContents(courseId))
                .isInstanceOf(ContentAccessDeniedException.class);
    }
}
