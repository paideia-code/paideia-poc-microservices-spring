package io.paideia.content.service.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.paideia.content.service.model.entity.ContentEntity;

public interface ContentRepository extends MongoRepository<ContentEntity, UUID> {

    List<ContentEntity> findByCourseId(UUID courseId);
}
