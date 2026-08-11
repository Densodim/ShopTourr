package com.example.shoptourr.api.alert

import com.example.shoptourr.api.common.MoneyDto
import com.example.shoptourr.api.purchase.PurchaseCategory

enum class AlertSeverity { INFO, WARNING, CRITICAL }

enum class AlertType {
    PACE_HIGH,
    CATEGORY_OVERSPENT,
    BUDGET_ALMOST_GONE,
    BUDGET_EXCEEDED,
    DAILY_ALLOWANCE,
}

data class BudgetAlertDto(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val titleKey: String,
    val bodyKey: String,
    val params: Map<String, String> = emptyMap(),
    val dailyRemaining: MoneyDto? = null,
    val category: PurchaseCategory? = null,
    val createdAt: String,
    val read: Boolean,
)

data class TripAlertsResponse(
    val alerts: List<BudgetAlertDto>,
)
