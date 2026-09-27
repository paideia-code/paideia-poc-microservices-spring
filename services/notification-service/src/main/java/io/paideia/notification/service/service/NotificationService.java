package io.paideia.notification.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.notification.service.controller.dto.NotificationRequestDTO;
import io.paideia.notification.service.controller.dto.NotificationResponseDTO;
import io.paideia.notification.service.enums.NotificationStatus;
import io.paideia.notification.service.exception.custom.NotificationNotFoundException;
import io.paideia.notification.service.model.repository.NotificationRepository;
import io.paideia.notification.service.service.mapper.NotificationMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional(readOnly = true)
    public NotificationResponseDTO findById(UUID id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO dto, UUID studentId, String email) {

        /* Notifications persistance */
        var notification = notificationRepository.save(notificationMapper.toEntity(dto, studentId, email));

        /* ToEmail Validation */
        if (email == null || email.isBlank()) {
            log.warn("event=notification.email.missing notificationId={} studentId={} status=FAILED", notification.getId(), studentId);
            notification.setStatus(NotificationStatus.FAILED);
            return notificationMapper.toResponse(notificationRepository.save(notification));
        }

        /* Send mail */
        long sendStartedNanos = System.nanoTime();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(dto.subject());
            message.setText(dto.body());
            message.setFrom(fromEmail);
            log.debug("event=notification.email.send.started notificationId={} studentId={} type={}", notification.getId(), studentId, dto.type());
            mailSender.send(message);
            long durationMs = (System.nanoTime() - sendStartedNanos) / 1_000_000;
            log.info("event=notification.email.sent notificationId={} studentId={} type={} durationMs={}", notification.getId(), studentId, dto.type(), durationMs);

            notification.setStatus(NotificationStatus.SENT);
        } catch (MailException e) {
            long durationMs = (System.nanoTime() - sendStartedNanos) / 1_000_000;
            log.error("event=notification.email.failed notificationId={} studentId={} type={} durationMs={}", notification.getId(), studentId, dto.type(), durationMs, e);
            notification.setStatus(NotificationStatus.FAILED);
        }

        /* Notifications persistance result */
        var updated = notificationRepository.save(notification);
        log.info("event=notification.completed notificationId={} studentId={} status={}", updated.getId(), studentId, updated.getStatus());

        return notificationMapper.toResponse(updated);
    }
}
