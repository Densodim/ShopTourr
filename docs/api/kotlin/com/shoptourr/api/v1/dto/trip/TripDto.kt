package com.shoptourr.api.v1.dto.trip

import com.shoptourr.api.v1.dto.common.ExchangeRateDto
import com.shoptourr.api.v1.dto.common.MoneyDto
import jakarta.validation.Valid
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Full trip for detail screen. Counters are server-computed.
 */
data class TripDto(
    val id: UUID,
    val city: String,
    val country: String,
    /** ISO 3166-1 alpha-2 when known (PT, JP, NO). */
    val countryCode: String?,
    val flagEmoji: String?,
    val status: TripStatus,
    val startDate: LocalDate,
    val endDate: LocalDate,
    /** Display label from mock e.g. "12–19 APR" — optional client convenience. */
    val datesLabel: String?,
    @field:Valid
    val budget: MoneyDto,
    @field:Valid
    val spent: MoneyDto,
    @field:Valid
    val remaining: MoneyDto,
    val purchaseCount: Int,
    val dayCount: Int,
    /** 1-based current day within trip when ACTIVE; null otherwise. */
    val currentDayNumber: Int?,
    /** Country default VAT %, e.g. 23 for Portugal. */
    val defaultVatRatePercent: BigDecimal?,
    val exchangeRate: ExchangeRateDto?,
    val travelers: List<TravelerDto>?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
