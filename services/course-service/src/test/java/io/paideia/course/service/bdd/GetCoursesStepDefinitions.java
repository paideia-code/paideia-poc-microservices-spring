package io.paideia.course.service.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.paideia.course.service.controller.dto.CourseResponseDTO;
import io.paideia.course.service.enums.CourseStatus;
import io.paideia.course.service.model.entity.CourseEntity;
import io.paideia.course.service.model.repository.CourseRepository;
import io.paideia.course.service.service.CourseService;
import io.paideia.course.service.service.mapper.CourseMapper;

public class GetCoursesStepDefinitions {

    private CourseRepository courseRepository;
    private CourseService courseService;
    private List<CourseResponseDTO> response;
    private Exception error;

    @Before
    public void setUp() {
        courseRepository = mock(CourseRepository.class);
        courseService = new CourseService(courseRepository, new CourseMapper());
        response = List.of();
        error = null;
    }

    /* Scenario: Consultar cursos registrados */

    @Given("existen cursos registrados")
    public void existenCursosRegistrados() {
        CourseEntity published = CourseEntity.builder()
                .id(UUID.fromString("11111111-0000-0000-0000-000000000101"))
                .title("Introducción a Java")
                .description("Curso publicado disponible para inscripción.")
                .price(new BigDecimal("50.00"))
                .status(CourseStatus.PUBLISHED)
                .build();

        CourseEntity archived = CourseEntity.builder()
                .id(UUID.fromString("11111111-0000-0000-0000-000000000102"))
                .title("Java legacy")
                .description("Curso retirado de la oferta activa.")
                .price(new BigDecimal("25.00"))
                .status(CourseStatus.ARCHIVED)
                .build();

        when(courseRepository.findAll()).thenReturn(List.of(published, archived));
    }

    @When("cualquier actor consulta los cursos")
    public void cualquierActorConsultaLosCursos() {
        try {
            response = courseService.findAll();
        } catch (Exception ex) {
            error = ex;
        }
    }

    @Then("la plataforma muestra la lista de cursos con su estado")
    public void laPlataformaMuestraLaListaDeCursosConSuEstado() {
        assertThat(error).isNull();
        assertThat(response)
                .extracting(CourseResponseDTO::status)
                .containsExactly(CourseStatus.PUBLISHED, CourseStatus.ARCHIVED);
        assertThat(response)
                .extracting(CourseResponseDTO::title)
                .containsExactly("Introducción a Java", "Java legacy");
    }
}
