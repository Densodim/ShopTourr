package com.example.shoptourr.ui.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Names the day headers on the trip screen the way the design does. */
object TripDayLabel {
    /**
     * Catalog key for a purchase day, or null when the day is old enough that the
     * date itself reads better than a relative name. An unparseable date also
     * returns null so one bad row cannot break the list.
     */
    fun keyFor(date: String, today: LocalDate): String? {
        val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        return when (parsed) {
            today -> "today"
            today.minus(1, DateTimeUnit.DAY) -> "yesterday"
            else -> null
        }
    }
}
