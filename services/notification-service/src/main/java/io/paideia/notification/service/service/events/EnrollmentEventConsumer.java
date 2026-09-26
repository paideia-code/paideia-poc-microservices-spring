package io.paideia.notification.service.service.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.paideia.notification.service.controller.dto.NotificationRequestDTO;
import io.paideia.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "enrollment.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(EnrollmentCreatedEvent event) {
        log.info("event=enrollment.received eventId={} enrollmentId={} studentId={}", event.eventId(), event.enrollmentId(), event.studentId());

        NotificationRequestDTO notification = new NotificationRequestDTO(event.notificationType(), event.subject(), event.body());

        notificationService.create(notification, event.studentId(), event.recipientEmail());

        log.info("event=enrollment.notification.processed enrollmentId={}", event.enrollmentId());
    }
}
