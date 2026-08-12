package com.shoptourr.api.v1.dto.stats

import com.shoptourr.api.v1.dto.common.MoneyDto
import com.shoptourr.api.v1.dto.purchase.PurchaseCategory
import java.math.BigDecimal

data class CategorySpendDto(
    val category: PurchaseCategory,
    val amount: MoneyDto,
    /** 0..1 share of total. */
    val share: BigDecimal,
    val purchaseCount: Int,
)
