package com.example.shoptourr.data.remote.dto.alert

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory
import kotlinx.serialization.Serializable

@Serializable
enum class AlertSeverity { INFO, WARNING, CRITICAL }

@Serializable
enum class AlertType {
    PACE_HIGH,
    CATEGORY_OVERSPENT,
    BUDGET_ALMOST_GONE,
    BUDGET_EXCEEDED,
    DAILY_ALLOWANCE,
}

@Serializable
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

@Serializable
data class TripAlertsResponse(
    val alerts: List<BudgetAlertDto>,
)
