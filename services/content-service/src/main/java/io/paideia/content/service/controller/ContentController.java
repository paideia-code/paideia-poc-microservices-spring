package io.paideia.content.service.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import io.paideia.content.service.controller.dto.ContentResponseDTO;
import io.paideia.content.service.exception.custom.InvalidContentUploadException;
import io.paideia.content.service.service.ContentService;
import io.paideia.content.service.service.ContentUploadCommand;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private static final String STUDENT_ID_HEADER = "X-Student-Id";
    private static final TypeReference<Map<String, Object>> METADATA_TYPE = new TypeReference<>() {};

    private final ContentService contentService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentResponseDTO> create(@RequestParam UUID courseId, @RequestParam(required = false) String description, @RequestParam(required = false) String metadata, @RequestPart("file") MultipartFile file) {
        ContentResponseDTO response = contentService.create(toCommand(courseId, description, metadata, file));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(response);

    }

    private ContentUploadCommand toCommand(UUID courseId, String description, String metadata, MultipartFile file) {
        try {
            return new ContentUploadCommand(
                    courseId,
                    filename(file),
                    contentType(file),
                    file.getBytes(),
                    description,
                    metadata(metadata));
        } catch (IOException ex) {
            throw new InvalidContentUploadException("Could not read content file");
        }
    }

    private Map<String, Object> metadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(metadata, METADATA_TYPE);
            return parsed == null ? Map.of() : parsed;
        } catch (JacksonException ex) {
            throw new InvalidContentUploadException("Metadata must be a valid JSON object");
        }
    }

    private String filename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return originalFilename == null || originalFilename.isBlank()
                ? "content"
                : originalFilename;
    }

    private String contentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : contentType;
    }

    @GetMapping
    public ResponseEntity<List<ContentResponseDTO>> findPurchasedCourseContents(@RequestParam UUID courseId, @RequestHeader(STUDENT_ID_HEADER) UUID studentId) {
        return ResponseEntity.ok(contentService.findPurchasedCourseContents(courseId, studentId));
    }
}
