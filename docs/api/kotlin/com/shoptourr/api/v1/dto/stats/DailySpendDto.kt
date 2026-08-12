package com.shoptourr.api.v1.dto.stats

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.time.LocalDate

data class DailySpendDto(
    val date: LocalDate,
    val amount: MoneyDto,
    val purchaseCount: Int,
)
