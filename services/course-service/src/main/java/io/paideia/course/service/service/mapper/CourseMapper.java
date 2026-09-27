package io.paideia.course.service.service.mapper;

import org.springframework.stereotype.Component;

import io.paideia.course.service.controller.dto.CourseRequestDTO;
import io.paideia.course.service.controller.dto.CourseResponseDTO;
import io.paideia.course.service.model.entity.CourseEntity;

@Component
public class CourseMapper {

    public CourseEntity toEntity(CourseRequestDTO dto) {
        return CourseEntity.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .status(dto.status())
                .build();
    }

    public CourseResponseDTO toResponse(CourseEntity entity) {
        return new CourseResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
