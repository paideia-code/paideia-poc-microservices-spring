package io.paideia.payment.service.service.mapper;

import org.springframework.stereotype.Component;

import io.paideia.payment.service.controller.dto.PaymentRequestDTO;
import io.paideia.payment.service.controller.dto.PaymentResponseDTO;
import io.paideia.payment.service.enums.PaymentStatus;
import io.paideia.payment.service.model.entity.PaymentEntity;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(PaymentRequestDTO dto, PaymentStatus status, String failureReason) {
        return PaymentEntity.builder()
                .amount(dto.amount())
                .status(status)
                .failureReason(failureReason)
                .build();
    }

    public PaymentResponseDTO toResponse(PaymentEntity entity) {
        return new PaymentResponseDTO(
                entity.getId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
