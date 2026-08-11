package com.example.shoptourr.data.remote.dto.stats

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory

data class CategorySpendDto(
    val category: PurchaseCategory,
    val amount: MoneyDto,
    val share: String,
    val purchaseCount: Int,
)

data class DailySpendDto(
    val date: String,
    val amount: MoneyDto,
    val purchaseCount: Int,
)

data class TripStatsDto(
    val tripId: String,
    val totalSpent: MoneyDto,
    val budget: MoneyDto,
    val dailyAverage: MoneyDto,
    val remaining: MoneyDto,
    val onBudget: Boolean,
    val paceDeltaDays: Int? = null,
    val topCategory: PurchaseCategory? = null,
    val byCategory: List<CategorySpendDto>,
    val byDay: List<DailySpendDto>,
)
