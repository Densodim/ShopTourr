package com.example.shoptourr.api.taxfree

import com.example.shoptourr.api.common.MoneyDto

data class TaxFreeRulesDto(
    val currency: String,
    val minimumPurchase: MoneyDto,
    val estimatedRefundRate: String,
    val regionLabel: String,
)

data class TaxFreeEligibleItemDto(
    val purchaseId: String,
    val name: String,
    val amount: MoneyDto,
    val estimatedRefund: MoneyDto,
    val meetsMinimum: Boolean,
)

data class TaxFreeSummaryDto(
    val tripId: String,
    val rules: TaxFreeRulesDto,
    val eligibleCount: Int,
    val eligibleTotal: MoneyDto,
    val estimatedRefundTotal: MoneyDto,
    val remainingToMinimum: MoneyDto? = null,
    val items: List<TaxFreeEligibleItemDto>,
)
