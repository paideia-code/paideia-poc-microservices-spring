package io.paideia.user.service.model.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.paideia.user.service.model.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
