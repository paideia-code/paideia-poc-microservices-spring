package io.paideia.course.service.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.paideia.course.service.controller.dto.CourseRequestDTO;
import io.paideia.course.service.controller.dto.CourseResponseDTO;
import io.paideia.course.service.enums.CourseStatus;
import io.paideia.course.service.exception.custom.CourseTitleConflictException;
import io.paideia.course.service.model.entity.CourseEntity;
import io.paideia.course.service.model.repository.CourseRepository;
import io.paideia.course.service.service.CourseService;
import io.paideia.course.service.service.mapper.CourseMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class CreateCoursesStepDefinitions {

    private CourseRepository courseRepository;
    private CourseService courseService;
    private Validator validator;
    private CourseResponseDTO response;
    private Exception error;
    private String title;

    @Before
    public void setUp() {
        courseRepository = mock(CourseRepository.class);
        courseService = new CourseService(courseRepository, new CourseMapper());
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        response = null;
        error = null;
        title = null;
    }

    /* Scenario: Crear curso con título nuevo */

    @Given("no existe un curso con título {string}")
    public void noExisteUnCursoConTitulo(String title) {
        this.title = title;
        when(courseRepository.existsByTitle(title)).thenReturn(false);
    }

    @When("el administrador registra un curso publicado con precio {bigdecimal}")
    public void elAdministradorRegistraUnCursoPublicadoConPrecio(BigDecimal price) {
        createCourse(title, price, CourseStatus.PUBLISHED);
    }

    @Then("el curso queda disponible para consulta")
    public void elCursoQuedaDisponibleParaConsulta() {
        assertThat(error).isNull();
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo(title);
        assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
        verify(courseRepository).save(any(CourseEntity.class));
    }

    /* Scenario: Rechazar curso con título duplicado */

    @Given("existe un curso con título {string}")
    public void existeUnCursoConTitulo(String title) {
        this.title = title;
        when(courseRepository.existsByTitle(title)).thenReturn(true);
    }

    @When("el administrador registra otro curso con título {string}")
    public void elAdministradorRegistraOtroCursoConTitulo(String title) {
        createCourse(title, new BigDecimal("149.99"), CourseStatus.PUBLISHED);
    }
    
    @Then("la plataforma rechaza la operación por título duplicado")
    public void laPlataformaRechazaLaOperacionPorTituloDuplicado() {
        assertThat(error).isInstanceOf(CourseTitleConflictException.class);
    }

    /* Scenario: Rechazar curso con precio negativo */

    @When("el administrador registra el curso con precio {bigdecimal}")
    public void elAdministradorRegistraElCursoConPrecio(BigDecimal price) {
        createCourse(title, price, CourseStatus.PUBLISHED);
    }

    @Then("la plataforma rechaza la operación por precio inválido")
    public void laPlataformaRechazaLaOperacionPorPrecioInvalido() {
        assertThat(error).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("precio");
    }


    private void createCourse(String title, BigDecimal price, CourseStatus status) {
        try {
            CourseRequestDTO request = new CourseRequestDTO(title, "Curso creado desde un escenario Cucumber.", price, status);
            validate(request);
            when(courseRepository.save(any(CourseEntity.class))).thenAnswer(invocation -> {
                CourseEntity entity = invocation.getArgument(0);
                entity.setId(UUID.fromString("11111111-0000-0000-0000-000000000099"));
                return entity;
            });
            response = courseService.create(request);
        } catch (Exception ex) {
            error = ex;
        }
    }

    private void validate(CourseRequestDTO request) {
        Set<ConstraintViolation<CourseRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new IllegalArgumentException(message);
        }
    }
}
