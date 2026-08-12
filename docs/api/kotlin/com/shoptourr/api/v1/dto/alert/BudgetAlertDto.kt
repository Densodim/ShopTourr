package com.shoptourr.api.v1.dto.alert

import com.shoptourr.api.v1.dto.common.MoneyDto
import com.shoptourr.api.v1.dto.purchase.PurchaseCategory
import java.time.Instant
import java.util.UUID

data class BudgetAlertDto(
    val id: UUID,
    val type: AlertType,
    val severity: AlertSeverity,
    val titleKey: String,
    val bodyKey: String,
    /** Interpolation vars for client i18n, e.g. {"days":"2","category":"FOOD"}. */
    val params: Map<String, String>?,
    val dailyRemaining: MoneyDto?,
    val category: PurchaseCategory?,
    val createdAt: Instant,
    val read: Boolean,
)
