package com.shoptourr.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_splits")
public class PurchaseSplitEntity {

    @Id
    private UUID id;

    @Column(name = "purchase_id", nullable = false)
    private UUID purchaseId;

    @Column(name = "traveler_id", nullable = false)
    private UUID travelerId;

    @Column(name = "share_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shareAmount;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPurchaseId() { return purchaseId; }
    public void setPurchaseId(UUID purchaseId) { this.purchaseId = purchaseId; }
    public UUID getTravelerId() { return travelerId; }
    public void setTravelerId(UUID travelerId) { this.travelerId = travelerId; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
}
