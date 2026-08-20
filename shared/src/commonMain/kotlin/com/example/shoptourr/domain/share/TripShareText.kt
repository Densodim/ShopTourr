package com.example.shoptourr.domain.share

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.i18n.VoyageI18n

/**
 * Plain-text card for the system share sheet. Amounts stay ISO (`120.00 EUR`) so the
 * same string is readable in Messages, Telegram and a notes app without Compose.
 */
object TripShareText {
    fun of(detail: TripDetail): String {
        val trip = detail.trip
        val locale = VoyageI18n.currentLocale
        val remaining = detail.remaining()
        val remainingLine = if (detail.isOverBudget()) {
            VoyageI18n.t(locale, "share_over_by", mapOf("amount" to money(remaining.abs())))
        } else {
            VoyageI18n.t(locale, "share_left", mapOf("amount" to money(remaining)))
        }
        return buildString {
            appendLine("${trip.city}, ${trip.country}")
            appendLine("${trip.startDate} → ${trip.endDate}")
            appendLine("${detail.spentTotal.toDecimalString()} / ${trip.budget.toDecimalString()} ${trip.budget.currency}")
            appendLine(remainingLine)
            append(VoyageI18n.plural(locale, "purchases", detail.purchases.size))
        }
    }

    private fun money(value: Money): String = "${value.toDecimalString()} ${value.currency}"
}

private fun Money.abs(): Money = if (minorUnits < 0) Money(-minorUnits, currency) else this
