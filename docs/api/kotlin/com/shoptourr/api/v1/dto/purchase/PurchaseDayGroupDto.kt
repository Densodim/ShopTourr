package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.time.LocalDate

/** Day bucket for trip timeline (today / yesterday / concrete date). */
data class PurchaseDayGroupDto(
    val date: LocalDate,
    val labelKey: String?,
    val dayTotal: MoneyDto,
    val items: List<PurchaseDto>,
)
