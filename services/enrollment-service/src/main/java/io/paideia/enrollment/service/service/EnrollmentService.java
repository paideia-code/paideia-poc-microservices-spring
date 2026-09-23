package io.paideia.enrollment.service.service;

import java.util.List;
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
    public EnrollmentAccessResponseDTO access(UUID studentId, UUID courseId) {
        boolean accessAllowed = enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(
                courseId, studentId, EnrollmentStatus.ENROLLED);
        return new EnrollmentAccessResponseDTO(studentId, courseId, accessAllowed);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> me(UUID studentId) {
        return enrollmentMapper.toResponse(enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ENROLLED));
    }

    @Transactional
    public EnrollmentResponseDTO enroll(UUID studentId, String paymentSimulation, EnrollmentRequestDTO dto) {
        /* Preconditions */
        CourseClientDTO course = courseClient.findById(dto.courseId());
        if (!"PUBLISHED".equals(course.status())) {
            log.warn("event=enrollment.course.unavailable courseId={} status={}", dto.courseId(), course.status());
            throw new CourseNotAvailableException(dto.courseId(), "Course status is " + course.status() + ", expected PUBLISHED");
        }

        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(dto.courseId(), studentId, EnrollmentStatus.ENROLLED)) {
            log.warn("event=enrollment.duplicate courseId={} studentId={}", dto.courseId(), studentId);
            throw new EnrollmentAlreadyExistsException(dto.courseId(), studentId);
        }

        /* Payments */
        PaymentClientResponseDTO payment = paymentClient.process(new PaymentClientRequestDTO(course.price()), paymentSimulation);
        log.info("event=enrollment.payment.processed courseId={} studentId={} paymentId={} paymentStatus={}", dto.courseId(), studentId, payment.id(), payment.status());

        /* Enrollments persistence */
        EnrollmentStatus status = "APPROVED".equals(payment.status()) ? EnrollmentStatus.ENROLLED : EnrollmentStatus.REJECTED;

        EnrollmentEntity saved = enrollmentRepository.save(EnrollmentEntity.builder()
                .courseId(dto.courseId())
                .studentId(studentId)
                .status(status)
                .paymentId(payment.id())
                .build());

        log.info("event=enrollment.created enrollmentId={} courseId={} studentId={} status={} paymentId={}", saved.getId(), saved.getCourseId(), saved.getStudentId(), saved.getStatus(), saved.getPaymentId());

        /* Notifications */
        String notifType = status == EnrollmentStatus.ENROLLED ? "ENROLLMENT_CONFIRMED" : "ENROLLMENT_REJECTED";
        String notifSubject = status == EnrollmentStatus.ENROLLED ? "Inscripcion confirmada" : "Inscripcion rechazada";
        notificationClient.notify(new NotificationClientRequestDTO(notifType, notifSubject, "Tu inscripcion en el curso " + course.title() + " fue procesada."));

        return enrollmentMapper.toResponse(saved);
    }
}
