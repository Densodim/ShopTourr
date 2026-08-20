package com.example.shoptourr.domain.widget

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BudgetWidgetCopy {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun of(
        home: HomeSnapshot,
        locale: AppLocale = VoyageI18n.currentLocale,
    ): BudgetWidgetSnapshot {
        val trip = home.currentTrip
        if (trip == null) {
            return BudgetWidgetSnapshot(
                tripId = null,
                city = VoyageI18n.t(locale, "widget_no_trip"),
                remainingLine = VoyageI18n.t(locale, "widget_no_trip_sub"),
                overBudget = false,
            )
        }
        val remaining = trip.remaining
        val remainingLine = if (trip.isOverBudget) {
            VoyageI18n.t(locale, "share_over_by", mapOf("amount" to money(remaining.abs())))
        } else {
            VoyageI18n.t(locale, "share_left", mapOf("amount" to money(remaining)))
        }
        return BudgetWidgetSnapshot(
            tripId = trip.id,
            city = trip.city,
            remainingLine = remainingLine,
            overBudget = trip.isOverBudget,
        )
    }

    fun encode(snapshot: BudgetWidgetSnapshot): String = json.encodeToString(snapshot)

    fun decode(raw: String?): BudgetWidgetSnapshot? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        return runCatching { json.decodeFromString<BudgetWidgetSnapshot>(value) }.getOrNull()
    }

    private fun money(value: Money): String = "${value.toDecimalString()} ${value.currency}"

    private fun Money.abs(): Money = if (minorUnits < 0) Money(-minorUnits, currency) else this
}
