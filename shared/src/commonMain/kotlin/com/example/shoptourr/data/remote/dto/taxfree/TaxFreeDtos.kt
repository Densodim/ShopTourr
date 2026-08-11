package com.example.shoptourr.data.remote.dto.taxfree

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import kotlinx.serialization.Serializable

@Serializable
data class TaxFreeRulesDto(
    val currency: String,
    val minimumPurchase: MoneyDto,
    val estimatedRefundRate: String,
    val regionLabel: String,
)

@Serializable
data class TaxFreeEligibleItemDto(
    val purchaseId: String,
    val name: String,
    val amount: MoneyDto,
    val estimatedRefund: MoneyDto,
    val meetsMinimum: Boolean,
)

@Serializable
data class TaxFreeSummaryDto(
    val tripId: String,
    val rules: TaxFreeRulesDto,
    val eligibleCount: Int,
    val eligibleTotal: MoneyDto,
    val estimatedRefundTotal: MoneyDto,
    val remainingToMinimum: MoneyDto? = null,
    val items: List<TaxFreeEligibleItemDto>,
)
