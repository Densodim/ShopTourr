package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto

data class TripPurchasesResponse(
    val spentTotal: MoneyDto,
    val budget: MoneyDto,
    val remaining: MoneyDto,
    val days: List<PurchaseDayGroupDto>,
)
