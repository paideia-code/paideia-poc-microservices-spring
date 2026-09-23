package io.paideia.content.service.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.paideia.content.service.client.EnrollmentAccessClient;
import io.paideia.content.service.controller.dto.ContentResponseDTO;
import io.paideia.content.service.exception.custom.ContentAccessDeniedException;
import io.paideia.content.service.model.entity.ContentEntity;
import io.paideia.content.service.model.repository.ContentRepository;
import io.paideia.content.service.service.ContentService;
import io.paideia.content.service.service.mapper.ContentMapper;

public class AccessPurchasedContentStepDefinitions {

    private static final UUID COURSE_ID = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID STUDENT_ID = UUID.fromString("33333333-0000-0000-0000-000000000001");

    private ContentRepository contentRepository;
    private EnrollmentAccessClient enrollmentAccessClient;
    private ContentService contentService;
    private List<ContentResponseDTO> responses;
    private Exception error;
    private UUID courseId;

    @Before
    public void setUp() {
        contentRepository = mock(ContentRepository.class);
        enrollmentAccessClient = mock(EnrollmentAccessClient.class);
        contentService = new ContentService(contentRepository, new ContentMapper(), enrollmentAccessClient);
        responses = null;
        error = null;
        courseId = null;
    }

   
    @Given("el estudiante tiene una inscripción {string} en el curso")
    public void elEstudianteTieneUnaInscripcionEnElCurso(String status) {
        courseId = COURSE_ID;
        when(enrollmentAccessClient.canAccessContent(courseId)).thenReturn("ENROLLED".equals(status));
    }

    @Given("el estudiante no tiene una inscripción {string} en el curso")
    public void elEstudianteNoTieneUnaInscripcionEnElCurso(String status) {
        courseId = COURSE_ID;
        when(enrollmentAccessClient.canAccessContent(courseId)).thenReturn(false);
    }

    @Given("el curso tiene materiales registrados")
    public void elCursoTieneMaterialesRegistrados() {
        ContentEntity content = ContentEntity.builder()
                .id(UUID.fromString("22222222-0000-0000-0000-000000000001"))
                .courseId(courseId)
                .filename("introduccion.pdf")
                .contentType("application/pdf")
                .sizeBytes(100L)
                .storageKey("courses/%s/contents/22222222-0000-0000-0000-000000000001/introduccion.pdf".formatted(courseId))
                .description("Material base")
                .metadata(Map.<String, Object>of("kind", "syllabus"))
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(contentRepository.findByCourseId(courseId)).thenReturn(List.of(content));
    }

    @Given("el header {string} identifica al estudiante")
    public void elHeaderIdentificaAlEstudiante(String headerName) {
        assertThat(headerName).isEqualTo("X-Student-Id");
    }

    @When("el estudiante consulta el contenido del curso")
    public void elEstudianteConsultaElContenidoDelCurso() {
        try {
            responses = contentService.findPurchasedCourseContents(courseId);
        } catch (Exception ex) {
            error = ex;
        }
    }

    @Then("la plataforma muestra los materiales del curso")
    public void laPlataformaMuestraLosMaterialesDelCurso() {
        assertThat(error).isNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().filename()).isEqualTo("introduccion.pdf");
    }

    @Then("la plataforma rechaza la operación por permisos insuficientes")
    public void laPlataformaRechazaLaOperacionPorPermisosInsuficientes() {
        assertThat(error).isInstanceOf(ContentAccessDeniedException.class);
    }
}
