package io.paideia.notification.service.service.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.paideia.notification.service.controller.dto.NotificationRequestDTO;
import io.paideia.notification.service.controller.dto.NotificationResponseDTO;
import io.paideia.notification.service.model.entity.NotificationEntity;

@Component
public class NotificationMapper {

    public NotificationEntity toEntity(NotificationRequestDTO dto, UUID studentId, String email) {
        return NotificationEntity.builder()
                .studentId(studentId)
                .email(email)
                .type(dto.type())
                .subject(dto.subject())
                .body(dto.body())
                .build();
    }

    public NotificationResponseDTO toResponse(NotificationEntity entity) {
        return new NotificationResponseDTO(
                entity.getId(),
                entity.getStudentId(),
                entity.getEmail(),
                entity.getType(),
                entity.getSubject(),
                entity.getBody(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
