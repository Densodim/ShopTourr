package com.shoptourr.api.v1.dto.taxfree

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.math.BigDecimal

data class TaxFreeRulesDto(
    val currency: String,
    val minimumPurchase: MoneyDto,
    /** Fraction e.g. 0.13 = 13% estimated refund. */
    val estimatedRefundRate: BigDecimal,
    val regionLabel: String,
)
