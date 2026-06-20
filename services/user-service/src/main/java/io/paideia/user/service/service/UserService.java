package io.paideia.user.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.user.service.controller.dto.StudentRegistrationRequestDTO;
import io.paideia.user.service.controller.dto.UserResponseDTO;
import io.paideia.user.service.exception.custom.UserEmailConflictException;
import io.paideia.user.service.exception.custom.UserNotFoundException;
import io.paideia.user.service.model.entity.UserEntity;
import io.paideia.user.service.model.repository.UserRepository;
import io.paideia.user.service.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public UserResponseDTO registerStudent(StudentRegistrationRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserEmailConflictException(dto.email());
        }
        UserEntity saved = userRepository.save(userMapper.toEntity(dto));
        return userMapper.toResponse(saved);
    }
}
