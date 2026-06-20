package io.paideia.course.service.controller.dto;

import java.math.BigDecimal;

import io.paideia.course.service.enums.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDTO(

        @NotBlank
        @Size(max = 200)
        String title,

        String description,

        @NotNull
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal price,

        @NotNull
        CourseStatus status

) {}
