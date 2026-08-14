package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyEntity.Key> {
    Optional<IdempotencyKeyEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}
