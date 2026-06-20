package io.paideia.enrollment.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.enrollment.service.client.CourseClient;
import io.paideia.enrollment.service.client.NotificationClient;
import io.paideia.enrollment.service.client.PaymentClient;
import io.paideia.enrollment.service.client.dto.CourseClientDTO;
import io.paideia.enrollment.service.client.dto.NotificationClientRequestDTO;
import io.paideia.enrollment.service.client.dto.PaymentClientRequestDTO;
import io.paideia.enrollment.service.client.dto.PaymentClientResponseDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentAccessResponseDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentRequestDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentResponseDTO;
import io.paideia.enrollment.service.enums.EnrollmentStatus;
import io.paideia.enrollment.service.exception.custom.CourseNotAvailableException;
import io.paideia.enrollment.service.exception.custom.EnrollmentAlreadyExistsException;
import io.paideia.enrollment.service.exception.custom.EnrollmentNotFoundException;
import io.paideia.enrollment.service.model.entity.EnrollmentEntity;
import io.paideia.enrollment.service.model.repository.EnrollmentRepository;
import io.paideia.enrollment.service.service.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseClient courseClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    @Transactional(readOnly = true)
    public EnrollmentResponseDTO findById(UUID id) {
        return enrollmentRepository.findById(id)
                .map(enrollmentMapper::toResponse)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public EnrollmentAccessResponseDTO canAccessContent(UUID studentId, UUID courseId) {
        boolean accessAllowed = enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(
                courseId, studentId, EnrollmentStatus.ENROLLED);
        return new EnrollmentAccessResponseDTO(studentId, courseId, accessAllowed);
    }

    @Transactional
    public EnrollmentResponseDTO enroll(UUID studentId, String paymentSimulation, EnrollmentRequestDTO dto) {
        CourseClientDTO course = courseClient.findById(dto.courseId());
        if (!"PUBLISHED".equals(course.status())) {
            throw new CourseNotAvailableException(dto.courseId(),
                    "Course status is " + course.status() + ", expected PUBLISHED");
        }

        if (enrollmentRepository.existsByCourseIdAndStudentId(dto.courseId(), studentId)) {
            throw new EnrollmentAlreadyExistsException(dto.courseId(), studentId);
        }

        PaymentClientResponseDTO payment = paymentClient.process(
                new PaymentClientRequestDTO(course.price()), paymentSimulation);

        EnrollmentStatus status = "APPROVED".equals(payment.status())
                ? EnrollmentStatus.ENROLLED
                : EnrollmentStatus.REJECTED;

        EnrollmentEntity saved = enrollmentRepository.save(EnrollmentEntity.builder()
                .courseId(dto.courseId())
                .studentId(studentId)
                .status(status)
                .paymentId(payment.id())
                .build());

        try {
            String notifType = status == EnrollmentStatus.ENROLLED
                    ? "ENROLLMENT_CONFIRMED" : "ENROLLMENT_REJECTED";
            String notifSubject = status == EnrollmentStatus.ENROLLED
                    ? "Inscripcion confirmada" : "Inscripcion rechazada";
            notificationClient.notify(new NotificationClientRequestDTO(
                    studentId, notifType, notifSubject,
                    "Tu inscripcion en el curso " + dto.courseId() + " fue procesada."));
        } catch (Exception ex) {
            log.warn("notification-service call failed (best-effort): {}", ex.getMessage());
        }

        return enrollmentMapper.toResponse(saved);
    }
}
