package com.shoptourr.api.v1.dto.trip;

import com.shoptourr.api.v1.dto.common.CommonDtos.ExchangeRateDto;
import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Trip aggregate — Home cards, Trip detail, New trip.
 */
public final class TripDtos {

    private TripDtos() {}

    public enum TripStatus {
        UPCOMING, ACTIVE, PAST, ARCHIVED
    }

    public record TravelerDto(
            UUID id,
            String name,
            /** Hex accent color e.g. #FFD84D */
            String colorHex,
            /** Single letter / initials for avatar glyph. */
            String avatarGlyph,
            boolean isOwner
    ) {}

    public record CreateTravelerRequest(
            @NotBlank @Size(min = 1, max = 60) String name,
            @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorHex,
            @Size(min = 1, max = 2) String avatarGlyph
    ) {}

    /**
     * Full trip for detail screen. Counters are server-computed.
     */
    public record TripDto(
            UUID id,
            String city,
            String country,
            /** ISO 3166-1 alpha-2 when known (PT, JP, NO). */
            String countryCode,
            String flagEmoji,
            TripStatus status,
            LocalDate startDate,
            LocalDate endDate,
            /** Display label from mock e.g. "12–19 APR" — optional client convenience. */
            String datesLabel,
            @Valid MoneyDto budget,
            @Valid MoneyDto spent,
            @Valid MoneyDto remaining,
            int purchaseCount,
            int dayCount,
            /** 1-based current day within trip when ACTIVE; null otherwise. */
            Integer currentDayNumber,
            /** Country default VAT %, e.g. 23 for Portugal. */
            BigDecimal defaultVatRatePercent,
            ExchangeRateDto exchangeRate,
            List<TravelerDto> travelers,
            Instant createdAt,
            Instant updatedAt
    ) {}

    /** Compact row for Home / archive lists. */
    public record TripSummaryDto(
            UUID id,
            String city,
            String country,
            String flagEmoji,
            TripStatus status,
            LocalDate startDate,
            LocalDate endDate,
            String datesLabel,
            MoneyDto budget,
            MoneyDto spent,
            int purchaseCount,
            Integer currentDayNumber,
            Integer dayCount
    ) {}

    public record CreateTripRequest(
            @NotBlank @Size(min = 1, max = 120) String city,
            @NotBlank @Size(min = 1, max = 120) String country,
            @Size(min = 2, max = 2) String countryCode,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull @Valid MoneyDto budget,
            /**
             * Optional; if null server resolves from countryCode (TaxFree/VAT tables)
             * or defaults to 0.
             */
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal defaultVatRatePercent,
            /**
             * Optional quote currency for FX snapshot (user preferred if null).
             */
            @Size(min = 3, max = 3) String quoteCurrency,
            List<@Valid CreateTravelerRequest> travelers
    ) {}

    public record UpdateTripRequest(
            @Size(min = 1, max = 120) String city,
            @Size(min = 1, max = 120) String country,
            @Size(min = 2, max = 2) String countryCode,
            LocalDate startDate,
            LocalDate endDate,
            @Valid MoneyDto budget,
            @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal defaultVatRatePercent,
            TripStatus status
    ) {}

    public record TripListResponse(
            List<TripSummaryDto> active,
            List<TripSummaryDto> upcoming,
            List<TripSummaryDto> past
    ) {}
}
