package com.example.shoptourr.domain.widget

import kotlinx.serialization.Serializable

@Serializable
data class BudgetWidgetSnapshot(
    val tripId: String? = null,
    val city: String,
    val remainingLine: String,
    val overBudget: Boolean = false,
)

object BudgetWidgetContract {
    const val PREFS_NAME = "voyage_widget"
    const val JSON_KEY = "budget_widget_json"
    const val APP_GROUP = "group.com.shoptourr"
}

interface BudgetWidgetStore {
    fun write(snapshot: BudgetWidgetSnapshot)
    fun read(): BudgetWidgetSnapshot?
}

fun interface BudgetWidgetRefresher {
    fun reload()
}
