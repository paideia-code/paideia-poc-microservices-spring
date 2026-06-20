package io.paideia.user.service.service.mapper;

import org.springframework.stereotype.Component;

import io.paideia.user.service.controller.dto.StudentRegistrationRequestDTO;
import io.paideia.user.service.controller.dto.UserResponseDTO;
import io.paideia.user.service.enums.UserRole;
import io.paideia.user.service.model.entity.UserEntity;

@Component
public class UserMapper {

    public UserEntity toEntity(StudentRegistrationRequestDTO dto) {
        return UserEntity.builder()
                .email(dto.email())
                .name(dto.name())
                .role(UserRole.STUDENT)
                .build();
    }

    public UserResponseDTO toResponse(UserEntity entity) {
        return new UserResponseDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
