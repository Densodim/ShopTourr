package com.shoptourr.api.v1.dto.purchase;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.common.CommonDtos.VatBreakdownDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Purchases — Trip timeline + Add purchase screen (incl. VAT, split, Tax Free flag, photo).
 */
public final class PurchaseDtos {

    private PurchaseDtos() {}

    public enum PurchaseCategory {
        FOOD,
        TRANSPORT,
        SOUVENIRS,
        HOTEL,
        CULTURE,
        OTHER
    }

    public record SplitShareDto(
            UUID travelerId,
            String travelerName,
            MoneyDto share
    ) {}

    public record PurchaseDto(
            UUID id,
            UUID tripId,
            String name,
            PurchaseCategory category,
            /** Gross amount in trip currency (always). */
            MoneyDto amount,
            VatBreakdownDto vat,
            boolean taxRefundEligible,
            String place,
            LocalDate purchaseDate,
            LocalTime purchaseTime,
            UUID receiptMediaId,
            String receiptThumbnailUrl,
            List<UUID> splitWithTravelerIds,
            List<SplitShareDto> splits,
            /** Your share when splits present; else equals amount. */
            MoneyDto yourShare,
            /** amount × trip.exchangeRate.rate in quote currency. */
            MoneyDto quoteEquivalent,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CreatePurchaseRequest(
            @NotBlank @Size(min = 1, max = 200) String name,
            @NotNull PurchaseCategory category,
            /**
             * Input amount as entered by user. Interpretation depends on {@code vatIncluded}:
             * - true  → this is gross
             * - false → this is net; server adds VAT to store gross
             */
            @NotNull @Valid MoneyDto amount,
            boolean vatIncluded,
            /**
             * Override trip default VAT; null → use trip.defaultVatRatePercent.
             */
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal vatRatePercent,
            boolean taxRefundEligible,
            @Size(max = 200) String place,
            /** Defaults to today (trip tz / UTC date) if null. */
            LocalDate purchaseDate,
            /** Defaults to now if null. */
            LocalTime purchaseTime,
            UUID receiptMediaId,
            /**
             * Traveler ids participating in split. Empty/null → owner only.
             * Must include at least the current user traveler when provided.
             */
            List<UUID> splitWithTravelerIds
    ) {}

    public record UpdatePurchaseRequest(
            @Size(min = 1, max = 200) String name,
            PurchaseCategory category,
            @Valid MoneyDto amount,
            Boolean vatIncluded,
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal vatRatePercent,
            Boolean taxRefundEligible,
            @Size(max = 200) String place,
            LocalDate purchaseDate,
            LocalTime purchaseTime,
            UUID receiptMediaId,
            List<UUID> splitWithTravelerIds
    ) {}

    /** Day bucket for trip timeline (today / yesterday / concrete date). */
    public record PurchaseDayGroupDto(
            LocalDate date,
            String labelKey,
            MoneyDto dayTotal,
            List<PurchaseDto> items
    ) {}

    public record TripPurchasesResponse(
            MoneyDto spentTotal,
            MoneyDto budget,
            MoneyDto remaining,
            List<PurchaseDayGroupDto> days
    ) {}
}
