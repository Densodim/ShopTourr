package com.shoptourr.api.v1.dto.taxfree

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.util.UUID

data class TaxFreeSummaryDto(
    val tripId: UUID,
    val rules: TaxFreeRulesDto,
    val eligibleCount: Int,
    val eligibleTotal: MoneyDto,
    val estimatedRefundTotal: MoneyDto,
    /** How much more spend needed to unlock next eligible item / form, if any. */
    val remainingToMinimum: MoneyDto?,
    val items: List<TaxFreeEligibleItemDto>,
)
