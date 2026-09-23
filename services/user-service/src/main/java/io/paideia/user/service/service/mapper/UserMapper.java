package io.paideia.user.service.service.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.paideia.user.service.controller.dto.UserResponseDTO;
import io.paideia.user.service.model.entity.UserEntity;

@Component
public class UserMapper {

    public UserEntity toEntity(UUID keycloakId, String displayName) {
        return UserEntity.builder()
                .keycloakId(keycloakId)
                .displayName(displayName)
                .build();
    }

    public UserResponseDTO toResponse(UserEntity entity) {
        return new UserResponseDTO(
                entity.getKeycloakId(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
