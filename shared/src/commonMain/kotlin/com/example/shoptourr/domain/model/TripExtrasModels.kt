package com.example.shoptourr.domain.model

data class DiaryEntry(
    val id: String,
    val tripId: String,
    val entryDate: String,
    val mood: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)

data class DiaryDayGroup(
    val date: String,
    val labelKey: String? = null,
    val entries: List<DiaryEntry>,
)

data class CreateDiaryDraft(
    val entryDate: String? = null,
    val mood: String,
    val text: String,
)

data class TaxFreeRules(
    val currency: String,
    val minimumPurchase: Money,
    val estimatedRefundRate: String,
    val regionLabel: String,
)

data class TaxFreeEligibleItem(
    val purchaseId: String,
    val name: String,
    val amount: Money,
    val estimatedRefund: Money,
    val meetsMinimum: Boolean,
)

data class TaxFreeSummary(
    val tripId: String,
    val rules: TaxFreeRules,
    val eligibleCount: Int,
    val eligibleTotal: Money,
    val estimatedRefundTotal: Money,
    val remainingToMinimum: Money? = null,
    val items: List<TaxFreeEligibleItem>,
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

enum class AlertType {
    PACE_HIGH,
    CATEGORY_OVERSPENT,
    BUDGET_ALMOST_GONE,
    BUDGET_EXCEEDED,
    DAILY_ALLOWANCE,
}

data class BudgetAlert(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val titleKey: String,
    val bodyKey: String,
    val params: Map<String, String> = emptyMap(),
    val dailyRemaining: Money? = null,
    val category: PurchaseCategory? = null,
    val createdAt: String,
    val read: Boolean,
)
