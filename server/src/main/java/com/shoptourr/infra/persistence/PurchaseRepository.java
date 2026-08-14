package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {
    List<PurchaseEntity> findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(UUID tripId);

    Optional<PurchaseEntity> findByIdAndTripIdAndDeletedAtIsNull(UUID id, UUID tripId);

    long countByTripIdAndDeletedAtIsNull(UUID tripId);
}
