package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseSplitRepository extends JpaRepository<PurchaseSplitEntity, UUID> {
    List<PurchaseSplitEntity> findByPurchaseId(UUID purchaseId);

    void deleteByPurchaseId(UUID purchaseId);
}
