package io.paideia.user.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.user.service.controller.dto.UserResponseDTO;
import io.paideia.user.service.exception.custom.UserNotFoundException;
import io.paideia.user.service.model.entity.UserEntity;
import io.paideia.user.service.model.repository.UserRepository;
import io.paideia.user.service.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID keycloakId) {
        return userRepository.findById(keycloakId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(keycloakId));
    }

    @Transactional
    public UserResponseDTO getOrCreate(UUID keycloakId, String displayName) {
        return userRepository.findById(keycloakId)
                .map(entity -> {
                    log.debug("event=user.profile.found userId={}", keycloakId);
                    return userMapper.toResponse(entity);
                })
                .orElseGet(() -> {
                    UserEntity saved = userRepository.save(userMapper.toEntity(keycloakId, displayName));
                    log.info("event=user.profile.created userId={}", saved.getKeycloakId());
                    return userMapper.toResponse(saved);
                });
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID keycloakId, String displayName) {
        UserEntity entity = userRepository.findById(keycloakId).orElseThrow(() -> new UserNotFoundException(keycloakId));
        entity.setDisplayName(displayName);
        UserEntity saved = userRepository.save(entity);
        log.info("event=user.profile.updated userId={}", saved.getKeycloakId());
        return userMapper.toResponse(saved);
    }
   
}
