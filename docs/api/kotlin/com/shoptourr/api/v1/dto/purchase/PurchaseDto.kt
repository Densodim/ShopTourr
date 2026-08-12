package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto
import com.shoptourr.api.v1.dto.common.VatBreakdownDto
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class PurchaseDto(
    val id: UUID,
    val tripId: UUID,
    val name: String,
    val category: PurchaseCategory,
    /** Gross amount in trip currency (always). */
    val amount: MoneyDto,
    val vat: VatBreakdownDto,
    val taxRefundEligible: Boolean,
    val place: String?,
    val purchaseDate: LocalDate,
    val purchaseTime: LocalTime?,
    val receiptMediaId: UUID?,
    val receiptThumbnailUrl: String?,
    val splitWithTravelerIds: List<UUID>?,
    val splits: List<SplitShareDto>?,
    /** Your share when splits present; else equals amount. */
    val yourShare: MoneyDto,
    /** amount × trip.exchangeRate.rate in quote currency. */
    val quoteEquivalent: MoneyDto?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
