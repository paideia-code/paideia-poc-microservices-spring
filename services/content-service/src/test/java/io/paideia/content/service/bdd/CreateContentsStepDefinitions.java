package io.paideia.content.service.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.paideia.content.service.client.EnrollmentAccessClient;
import io.paideia.content.service.controller.dto.ContentResponseDTO;
import io.paideia.content.service.model.entity.ContentEntity;
import io.paideia.content.service.model.repository.ContentRepository;
import io.paideia.content.service.service.ContentService;
import io.paideia.content.service.service.ContentUploadCommand;
import io.paideia.content.service.service.mapper.ContentMapper;

public class CreateContentsStepDefinitions {

    private static final UUID COURSE_ID = UUID.fromString("11111111-0000-0000-0000-000000000001");

    private ContentRepository contentRepository;
    private ContentService contentService;
    private ContentResponseDTO response;
    private UUID courseId;

    @Before
    public void setUp() {
        contentRepository = mock(ContentRepository.class);
        EnrollmentAccessClient enrollmentAccessClient = mock(EnrollmentAccessClient.class);
        contentService = new ContentService(contentRepository, new ContentMapper(), enrollmentAccessClient);
        response = null;
        courseId = null;
    }

    @Given("existe un curso publicado")
    public void existeUnCursoPublicado() {
        courseId = COURSE_ID;
    }

    @When("el administrador registra un material con nombre {string} y metadata flexible")
    public void elAdministradorRegistraUnMaterialConNombreYMetadataFlexible(String filename) {
        var command = new ContentUploadCommand(
                courseId,
                filename,
                "application/pdf",
                "PDF bytes".getBytes(StandardCharsets.UTF_8),
                "Material base",
                Map.<String, Object>of(
                        "kind", "syllabus",
                        "pages", 12));

        when(contentRepository.save(any(ContentEntity.class))).thenAnswer(invocation -> {
            ContentEntity entity = invocation.getArgument(0);
            entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return entity;
        });

        response = contentService.create(command);
    }

    @Then("el material queda asociado al curso")
    public void elMaterialQuedaAsociadoAlCurso() {
        assertThat(response).isNotNull();
        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.filename()).isEqualTo("introduccion.pdf");
        assertThat(response.metadata())
                .containsEntry("kind", "syllabus")
                .containsEntry("pages", 12);
        verify(contentRepository).save(any(ContentEntity.class));
    }
}
