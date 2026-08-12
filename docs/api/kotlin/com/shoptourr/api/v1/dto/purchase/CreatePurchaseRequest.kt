package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CreatePurchaseRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,

    @field:NotNull
    val category: PurchaseCategory,

    /**
     * Input amount as entered by user. Interpretation depends on [vatIncluded]:
     * - true  → this is gross
     * - false → this is net; server adds VAT to store gross
     */
    @field:NotNull
    @field:Valid
    val amount: MoneyDto,

    val vatIncluded: Boolean = false,

    /**
     * Override trip default VAT; null → use trip.defaultVatRatePercent.
     */
    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val vatRatePercent: BigDecimal? = null,

    val taxRefundEligible: Boolean = false,

    @field:Size(max = 200)
    val place: String? = null,

    /** Defaults to today (trip tz / UTC date) if null. */
    val purchaseDate: LocalDate? = null,

    /** Defaults to now if null. */
    val purchaseTime: LocalTime? = null,

    val receiptMediaId: UUID? = null,

    /**
     * Traveler ids participating in split. Empty/null → owner only.
     * Must include at least the current user traveler when provided.
     */
    val splitWithTravelerIds: List<UUID>? = null,
)
