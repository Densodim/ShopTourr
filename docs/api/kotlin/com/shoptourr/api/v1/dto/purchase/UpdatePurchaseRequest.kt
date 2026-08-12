package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class UpdatePurchaseRequest(
    @field:Size(min = 1, max = 200)
    val name: String? = null,

    val category: PurchaseCategory? = null,

    @field:Valid
    val amount: MoneyDto? = null,

    val vatIncluded: Boolean? = null,

    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    val vatRatePercent: BigDecimal? = null,

    val taxRefundEligible: Boolean? = null,

    @field:Size(max = 200)
    val place: String? = null,

    val purchaseDate: LocalDate? = null,

    val purchaseTime: LocalTime? = null,

    val receiptMediaId: UUID? = null,

    val splitWithTravelerIds: List<UUID>? = null,
)
