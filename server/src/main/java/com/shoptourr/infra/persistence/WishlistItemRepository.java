package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, UUID> {
    List<WishlistItemEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<WishlistItemEntity> findByIdAndUserId(UUID id, UUID userId);
}
