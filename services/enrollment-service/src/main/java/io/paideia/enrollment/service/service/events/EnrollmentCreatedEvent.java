package io.paideia.enrollment.service.service.events;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID enrollmentId,
    UUID studentId,
    UUID courseId,
    String courseTitle,
    String enrollmentStatus,
    String notificationType,
    String subject,
    String body,
    String recipientEmail
) {
    
}
