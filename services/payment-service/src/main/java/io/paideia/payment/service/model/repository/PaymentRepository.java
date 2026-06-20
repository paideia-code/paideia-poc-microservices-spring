package io.paideia.payment.service.model.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.paideia.payment.service.model.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}
