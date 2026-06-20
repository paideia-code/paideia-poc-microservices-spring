package io.paideia.payment.service.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.payment.service.controller.dto.PaymentRequestDTO;
import io.paideia.payment.service.controller.dto.PaymentResponseDTO;
import io.paideia.payment.service.enums.PaymentStatus;
import io.paideia.payment.service.exception.custom.InvalidPaymentSimulationException;
import io.paideia.payment.service.exception.custom.PaymentNotFoundException;
import io.paideia.payment.service.model.entity.PaymentEntity;
import io.paideia.payment.service.model.repository.PaymentRepository;
import io.paideia.payment.service.service.mapper.PaymentMapper;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final boolean simulationHeaderEnabled;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            @Value("${payments.simulation.header-enabled:true}") boolean simulationHeaderEnabled) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.simulationHeaderEnabled = simulationHeaderEnabled;
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO findById(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Transactional
    public PaymentResponseDTO process(PaymentRequestDTO dto, String paymentSimulation) {
        PaymentStatus status = resolveStatus(paymentSimulation);
        String failureReason = status == PaymentStatus.REJECTED
                ? "Payment rejected by local simulation"
                : null;

        PaymentEntity saved = paymentRepository.save(paymentMapper.toEntity(dto, status, failureReason));
        return paymentMapper.toResponse(saved);
    }

    private PaymentStatus resolveStatus(String paymentSimulation) {
        if (paymentSimulation == null || paymentSimulation.isBlank()) {
            return PaymentStatus.APPROVED;
        }
        if (!simulationHeaderEnabled) {
            throw new InvalidPaymentSimulationException(paymentSimulation);
        }

        try {
            return PaymentStatus.valueOf(paymentSimulation.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidPaymentSimulationException(paymentSimulation);
        }
    }
}
