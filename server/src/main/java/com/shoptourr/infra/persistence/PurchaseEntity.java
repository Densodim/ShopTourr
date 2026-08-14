package com.shoptourr.infra.persistence;

import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
public class PurchaseEntity {

    @Id
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseCategory category;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "vat_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal vatAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "vat_rate", nullable = false, precision = 6, scale = 3)
    private BigDecimal vatRate;

    @Column(name = "vat_included", nullable = false)
    private boolean vatIncluded;

    @Column(name = "tax_refund_eligible", nullable = false)
    private boolean taxRefundEligible;

    private String place;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_time")
    private LocalTime purchaseTime;

    @Column(name = "receipt_media_id")
    private UUID receiptMediaId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PurchaseCategory getCategory() { return category; }
    public void setCategory(PurchaseCategory category) { this.category = category; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public void setVatAmount(BigDecimal vatAmount) { this.vatAmount = vatAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
    public boolean isVatIncluded() { return vatIncluded; }
    public void setVatIncluded(boolean vatIncluded) { this.vatIncluded = vatIncluded; }
    public boolean isTaxRefundEligible() { return taxRefundEligible; }
    public void setTaxRefundEligible(boolean taxRefundEligible) { this.taxRefundEligible = taxRefundEligible; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalTime getPurchaseTime() { return purchaseTime; }
    public void setPurchaseTime(LocalTime purchaseTime) { this.purchaseTime = purchaseTime; }
    public UUID getReceiptMediaId() { return receiptMediaId; }
    public void setReceiptMediaId(UUID receiptMediaId) { this.receiptMediaId = receiptMediaId; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
