package io.paideia.notification.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.notification.service.controller.dto.NotificationRequestDTO;
import io.paideia.notification.service.controller.dto.NotificationResponseDTO;
import io.paideia.notification.service.exception.custom.NotificationNotFoundException;
import io.paideia.notification.service.model.repository.NotificationRepository;
import io.paideia.notification.service.service.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public NotificationResponseDTO findById(UUID id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO dto) {
        var saved = notificationRepository.save(notificationMapper.toEntity(dto));
        return notificationMapper.toResponse(saved);
    }
}
