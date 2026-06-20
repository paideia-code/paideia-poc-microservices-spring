package io.paideia.content.service.model.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentEntity {

    @Id
    private UUID id;

    @Field("course_id")
    private UUID courseId;

    private String filename;

    @Field("content_type")
    private String contentType;

    @Field("size_bytes")
    private Long sizeBytes;

    @Field("storage_key")
    private String storageKey;

    @Field("file_bytes")
    private byte[] fileBytes;

    private String description;

    private Map<String, Object> metadata;

    @Field("created_at")
    private Instant createdAt;
}
