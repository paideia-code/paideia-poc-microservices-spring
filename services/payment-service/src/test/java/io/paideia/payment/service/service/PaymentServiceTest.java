package io.paideia.payment.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.paideia.payment.service.controller.dto.PaymentRequestDTO;
import io.paideia.payment.service.enums.PaymentStatus;
import io.paideia.payment.service.exception.custom.InvalidPaymentSimulationException;
import io.paideia.payment.service.model.entity.PaymentEntity;
import io.paideia.payment.service.model.repository.PaymentRepository;
import io.paideia.payment.service.service.mapper.PaymentMapper;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, new PaymentMapper(), true);
    }

    @Test
    void processApprovesByDefaultWhenSimulationHeaderIsAbsent() {
        givenPaymentIsSaved();

        var response = paymentService.process(new PaymentRequestDTO(new BigDecimal("10000.00")), null);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.failureReason()).isNull();
    }

    @Test
    void processRejectsWhenSimulationHeaderRequestsRejection() {
        givenPaymentIsSaved();

        var response = paymentService.process(new PaymentRequestDTO(new BigDecimal("49.99")), "REJECTED");

        assertThat(response.status()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(response.failureReason()).isNotBlank();
    }

    @Test
    void processRejectsUnsupportedSimulationHeader() {
        assertThatThrownBy(() -> paymentService.process(
                new PaymentRequestDTO(new BigDecimal("49.99")), "PENDING"))
                .isInstanceOf(InvalidPaymentSimulationException.class);
    }

    private void givenPaymentIsSaved() {
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("44444444-0000-0000-0000-000000000001"));
            return entity;
        });
    }
}
