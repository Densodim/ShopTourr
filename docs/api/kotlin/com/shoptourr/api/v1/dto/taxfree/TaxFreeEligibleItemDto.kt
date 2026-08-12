package com.shoptourr.api.v1.dto.taxfree

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.util.UUID

data class TaxFreeEligibleItemDto(
    val purchaseId: UUID,
    val name: String,
    val amount: MoneyDto,
    val estimatedRefund: MoneyDto,
    val meetsMinimum: Boolean,
)
