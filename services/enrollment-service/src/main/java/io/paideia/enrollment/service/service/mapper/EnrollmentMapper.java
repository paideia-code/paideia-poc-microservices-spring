package io.paideia.enrollment.service.service.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.paideia.enrollment.service.controller.dto.EnrollmentResponseDTO;
import io.paideia.enrollment.service.model.entity.EnrollmentEntity;

@Component
public class EnrollmentMapper {

    public EnrollmentResponseDTO toResponse(EnrollmentEntity entity) {
        return new EnrollmentResponseDTO(
                entity.getId(),
                entity.getCourseId(),
                entity.getStudentId(),
                entity.getStatus(),
                entity.getPaymentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<EnrollmentResponseDTO> toResponse(List<EnrollmentEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}
