package io.paideia.course.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.paideia.course.service.controller.dto.CourseRequestDTO;
import io.paideia.course.service.enums.CourseStatus;
import io.paideia.course.service.exception.custom.CourseNotFoundException;
import io.paideia.course.service.exception.custom.CourseTitleConflictException;
import io.paideia.course.service.model.entity.CourseEntity;
import io.paideia.course.service.model.repository.CourseRepository;
import io.paideia.course.service.service.mapper.CourseMapper;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, new CourseMapper());
    }

    @Test
    void createPersistsCourseWhenTitleIsAvailable() {
        var request = new CourseRequestDTO(
                "Spring Boot desde cero",
                "Construccion de APIs REST con Spring Boot.",
                new BigDecimal("79.99"),
                CourseStatus.PUBLISHED);

        when(courseRepository.existsByTitle(request.title())).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(invocation -> {
            CourseEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("11111111-0000-0000-0000-000000000002"));
            return entity;
        });

        var response = courseService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.price()).isEqualByComparingTo(request.price());
        assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
        verify(courseRepository).save(any(CourseEntity.class));
    }

    @Test
    void createRejectsDuplicateTitle() {
        var request = new CourseRequestDTO(
                "Spring Boot desde cero",
                "Duplicado",
                new BigDecimal("79.99"),
                CourseStatus.PUBLISHED);

        when(courseRepository.existsByTitle(request.title())).thenReturn(true);

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(CourseTitleConflictException.class);
    }

    @Test
    void findByIdReturnsCourseWhenItExists() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000002");
        var existing = CourseEntity.builder()
                .id(courseId)
                .title("Spring Boot avanzado")
                .description("APIs REST, testing y despliegue.")
                .price(new BigDecimal("129.99"))
                .status(CourseStatus.PUBLISHED)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existing));

        var response = courseService.findById(courseId);

        assertThat(response.title()).isEqualTo("Spring Boot avanzado");
        assertThat(response.price()).isEqualByComparingTo("129.99");
        assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
    }

    @Test
    void findByIdRejectsUnknownCourse() {
        UUID courseId = UUID.fromString("11111111-0000-0000-0000-000000000002");

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(courseId))
                .isInstanceOf(CourseNotFoundException.class);
    }
}
