package io.paideia.notification.service.model.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.paideia.notification.service.model.entity.NotificationEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {}
