package io.paideia.notification.service.service.mapper;

import org.springframework.stereotype.Component;

import io.paideia.notification.service.controller.dto.NotificationRequestDTO;
import io.paideia.notification.service.controller.dto.NotificationResponseDTO;
import io.paideia.notification.service.model.entity.NotificationEntity;

@Component
public class NotificationMapper {

    public NotificationEntity toEntity(NotificationRequestDTO dto) {
        return NotificationEntity.builder()
                .recipientId(dto.recipientId())
                .type(dto.type())
                .subject(dto.subject())
                .body(dto.body())
                .build();
    }

    public NotificationResponseDTO toResponse(NotificationEntity entity) {
        return new NotificationResponseDTO(
                entity.getId(),
                entity.getRecipientId(),
                entity.getType(),
                entity.getSubject(),
                entity.getBody(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
