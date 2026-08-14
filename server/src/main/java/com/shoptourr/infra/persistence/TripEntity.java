package com.shoptourr.infra.persistence;

import com.shoptourr.api.v1.dto.trip.TripDtos.TripStatus;
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
import java.util.UUID;

@Entity
@Table(name = "trips")
public class TripEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "flag_emoji")
    private String flagEmoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "budget_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "budget_currency", nullable = false, length = 3)
    private String budgetCurrency;

    @Column(name = "default_vat_rate", nullable = false, precision = 6, scale = 3)
    private BigDecimal defaultVatRate;

    @Column(name = "fx_trip_currency", length = 3)
    private String fxTripCurrency;

    @Column(name = "fx_quote_currency", length = 3)
    private String fxQuoteCurrency;

    @Column(name = "fx_rate", precision = 19, scale = 8)
    private BigDecimal fxRate;

    @Column(name = "fx_rate_date")
    private LocalDate fxRateDate;

    @Column(name = "fx_provider")
    private String fxProvider;

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
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getFlagEmoji() { return flagEmoji; }
    public void setFlagEmoji(String flagEmoji) { this.flagEmoji = flagEmoji; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public String getBudgetCurrency() { return budgetCurrency; }
    public void setBudgetCurrency(String budgetCurrency) { this.budgetCurrency = budgetCurrency; }
    public BigDecimal getDefaultVatRate() { return defaultVatRate; }
    public void setDefaultVatRate(BigDecimal defaultVatRate) { this.defaultVatRate = defaultVatRate; }
    public String getFxTripCurrency() { return fxTripCurrency; }
    public void setFxTripCurrency(String fxTripCurrency) { this.fxTripCurrency = fxTripCurrency; }
    public String getFxQuoteCurrency() { return fxQuoteCurrency; }
    public void setFxQuoteCurrency(String fxQuoteCurrency) { this.fxQuoteCurrency = fxQuoteCurrency; }
    public BigDecimal getFxRate() { return fxRate; }
    public void setFxRate(BigDecimal fxRate) { this.fxRate = fxRate; }
    public LocalDate getFxRateDate() { return fxRateDate; }
    public void setFxRateDate(LocalDate fxRateDate) { this.fxRateDate = fxRateDate; }
    public String getFxProvider() { return fxProvider; }
    public void setFxProvider(String fxProvider) { this.fxProvider = fxProvider; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
