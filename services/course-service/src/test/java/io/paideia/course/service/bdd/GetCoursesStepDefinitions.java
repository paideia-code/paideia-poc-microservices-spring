package io.paideia.course.service.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    private Page<CourseResponseDTO> response;
    private Exception error;

    @Before
    public void setUp() {
        courseRepository = mock(CourseRepository.class);
        courseService = new CourseService(courseRepository, new CourseMapper());
        response = Page.empty();
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

        var pageable = PageRequest.of(0, 20);
        when(courseRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(published, archived), pageable, 2));
    }

    @When("cualquier actor consulta los cursos")
    public void cualquierActorConsultaLosCursos() {
        try {
            response = courseService.findAll(PageRequest.of(0, 20));
        } catch (Exception ex) {
            error = ex;
        }
    }

    @Then("la plataforma muestra la lista de cursos con su estado")
    public void laPlataformaMuestraLaListaDeCursosConSuEstado() {
        assertThat(error).isNull();
        assertThat(response.getContent())
                .extracting(CourseResponseDTO::status)
                .containsExactly(CourseStatus.PUBLISHED, CourseStatus.ARCHIVED);
        assertThat(response.getContent())
                .extracting(CourseResponseDTO::title)
                .containsExactly("Introducción a Java", "Java legacy");
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(20);
    }
}
