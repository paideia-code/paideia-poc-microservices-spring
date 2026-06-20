package io.paideia.content.service.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.paideia.content.service.model.entity.ContentEntity;
import io.paideia.content.service.model.repository.ContentRepository;

@Configuration
public class ContentSeedConfig {

    @Bean
    @ConditionalOnProperty(
            name = "paideia.seed.content.enabled",
            havingValue = "true",
            matchIfMissing = true)
    ApplicationRunner seedContents(ContentRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            repository.saveAll(List.of(
                    ContentEntity.builder()
                            .id(UUID.fromString("22222222-0000-0000-0000-000000000001"))
                            .courseId(UUID.fromString("11111111-0000-0000-0000-000000000001"))
                            .filename("java-introduccion-syllabus.pdf")
                            .contentType("application/pdf")
                            .sizeBytes(245760L)
                            .storageKey(storageKey(
                                    "11111111-0000-0000-0000-000000000001",
                                    "22222222-0000-0000-0000-000000000001",
                                    "java-introduccion-syllabus.pdf"))
                            .description("Programa del curso Introduccion a Java.")
                            .metadata(Map.<String, Object>of(
                                    "kind", "syllabus",
                                    "lessonNumber", 1,
                                    "pages", 12))
                            .createdAt(createdAt)
                            .build(),
                    ContentEntity.builder()
                            .id(UUID.fromString("22222222-0000-0000-0000-000000000002"))
                            .courseId(UUID.fromString("11111111-0000-0000-0000-000000000002"))
                            .filename("spring-boot-rest-api.pdf")
                            .contentType("application/pdf")
                            .sizeBytes(389120L)
                            .storageKey(storageKey(
                                    "11111111-0000-0000-0000-000000000002",
                                    "22222222-0000-0000-0000-000000000002",
                                    "spring-boot-rest-api.pdf"))
                            .description("Guia inicial para construir APIs REST con Spring Boot.")
                            .metadata(Map.<String, Object>of(
                                    "kind", "guide",
                                    "lessonNumber", 2,
                                    "pages", 24))
                            .createdAt(createdAt)
                            .build(),
                    ContentEntity.builder()
                            .id(UUID.fromString("22222222-0000-0000-0000-000000000003"))
                            .courseId(UUID.fromString("11111111-0000-0000-0000-000000000003"))
                            .filename("microservicios-arquitectura.md")
                            .contentType("text/markdown")
                            .sizeBytes(32768L)
                            .storageKey(storageKey(
                                    "11111111-0000-0000-0000-000000000003",
                                    "22222222-0000-0000-0000-000000000003",
                                    "microservicios-arquitectura.md"))
                            .description("Notas sobre bounded contexts, comunicacion HTTP y database-per-service.")
                            .metadata(Map.<String, Object>of(
                                    "kind", "notes",
                                    "format", "markdown",
                                    "tags", List.of("architecture", "microservices")))
                            .createdAt(createdAt)
                            .build()));
        };
    }

    private String storageKey(String courseId, String contentId, String filename) {
        return "courses/%s/contents/%s/%s".formatted(courseId, contentId, filename);
    }
}
