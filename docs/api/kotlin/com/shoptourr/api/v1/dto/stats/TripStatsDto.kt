package com.shoptourr.api.v1.dto.stats

import com.shoptourr.api.v1.dto.common.MoneyDto
import com.shoptourr.api.v1.dto.purchase.PurchaseCategory
import java.util.UUID

data class TripStatsDto(
    val tripId: UUID,
    val totalSpent: MoneyDto,
    val budget: MoneyDto,
    val dailyAverage: MoneyDto,
    val remaining: MoneyDto,
    val onBudget: Boolean,
    /** Positive days early/late vs linear burn; null if no dates. */
    val paceDeltaDays: Int?,
    val topCategory: PurchaseCategory?,
    val byCategory: List<CategorySpendDto>,
    val byDay: List<DailySpendDto>,
)
