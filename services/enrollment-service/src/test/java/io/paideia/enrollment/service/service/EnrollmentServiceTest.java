package io.paideia.enrollment.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.paideia.enrollment.service.client.CourseClient;
import io.paideia.enrollment.service.client.NotificationClient;
import io.paideia.enrollment.service.client.PaymentClient;
import io.paideia.enrollment.service.client.dto.CourseClientDTO;
import io.paideia.enrollment.service.client.dto.NotificationClientRequestDTO;
import io.paideia.enrollment.service.client.dto.PaymentClientRequestDTO;
import io.paideia.enrollment.service.client.dto.PaymentClientResponseDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentRequestDTO;
import io.paideia.enrollment.service.enums.EnrollmentStatus;
import io.paideia.enrollment.service.exception.custom.CourseNotAvailableException;
import io.paideia.enrollment.service.model.entity.EnrollmentEntity;
import io.paideia.enrollment.service.model.repository.EnrollmentRepository;
import io.paideia.enrollment.service.service.mapper.EnrollmentMapper;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    private static final UUID COURSE_ID = UUID.fromString("11111111-0000-0000-0000-000000000002");
    private static final UUID STUDENT_ID = UUID.fromString("33333333-0000-0000-0000-000000000001");
    private static final UUID PAYMENT_ID = UUID.fromString("44444444-0000-0000-0000-000000000001");
    private static final UUID ENROLLMENT_ID = UUID.fromString("55555555-0000-0000-0000-000000000001");

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseClient courseClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private NotificationClient notificationClient;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                new EnrollmentMapper(),
                courseClient,
                paymentClient,
                notificationClient);
    }

    @Test
    void enrollConfirmsEnrollmentAndNotifiesWhenPaymentIsApproved() {
        givenPublishedCourse();
        when(enrollmentRepository.existsByCourseIdAndStudentId(COURSE_ID, STUDENT_ID)).thenReturn(false);
        when(paymentClient.process(any(PaymentClientRequestDTO.class), eq("APPROVED")))
                .thenReturn(new PaymentClientResponseDTO(PAYMENT_ID, new BigDecimal("79.99"), "APPROVED"));
        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(invocation -> {
            EnrollmentEntity entity = invocation.getArgument(0);
            entity.setId(ENROLLMENT_ID);
            return entity;
        });

        var response = enrollmentService.enroll(STUDENT_ID, "APPROVED", new EnrollmentRequestDTO(COURSE_ID));

        assertThat(response.id()).isEqualTo(ENROLLMENT_ID);
        assertThat(response.status()).isEqualTo(EnrollmentStatus.ENROLLED);
        assertThat(response.paymentId()).isEqualTo(PAYMENT_ID);
        verify(notificationClient).notify(any(NotificationClientRequestDTO.class));
    }

    @Test
    void enrollRejectsEnrollmentAndNotifiesWhenPaymentIsRejected() {
        givenPublishedCourse();
        when(enrollmentRepository.existsByCourseIdAndStudentId(COURSE_ID, STUDENT_ID)).thenReturn(false);
        when(paymentClient.process(any(PaymentClientRequestDTO.class), eq("REJECTED")))
                .thenReturn(new PaymentClientResponseDTO(PAYMENT_ID, new BigDecimal("10000.00"), "REJECTED"));
        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(invocation -> {
            EnrollmentEntity entity = invocation.getArgument(0);
            entity.setId(ENROLLMENT_ID);
            return entity;
        });

        var response = enrollmentService.enroll(STUDENT_ID, "REJECTED", new EnrollmentRequestDTO(COURSE_ID));

        assertThat(response.status()).isEqualTo(EnrollmentStatus.REJECTED);
        verify(notificationClient).notify(any(NotificationClientRequestDTO.class));
    }

    @Test
    void enrollRejectsCourseThatIsNotPublished() {
        when(courseClient.findById(COURSE_ID))
                .thenReturn(new CourseClientDTO(COURSE_ID, "Docker y contenedores", new BigDecimal("59.99"), "DRAFT"));

        assertThatThrownBy(() -> enrollmentService.enroll(STUDENT_ID, null, new EnrollmentRequestDTO(COURSE_ID)))
                .isInstanceOf(CourseNotAvailableException.class);

        verify(paymentClient, never()).process(any(PaymentClientRequestDTO.class), any());
        verify(enrollmentRepository, never()).save(any(EnrollmentEntity.class));
    }

    @Test
    void canAccessContentAllowsEnrolledStudent() {
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(
                COURSE_ID, STUDENT_ID, EnrollmentStatus.ENROLLED)).thenReturn(true);

        var response = enrollmentService.canAccessContent(STUDENT_ID, COURSE_ID);

        assertThat(response.accessAllowed()).isTrue();
    }

    private void givenPublishedCourse() {
        when(courseClient.findById(COURSE_ID))
                .thenReturn(new CourseClientDTO(COURSE_ID, "Spring Boot desde cero", new BigDecimal("79.99"), "PUBLISHED"));
    }
}
