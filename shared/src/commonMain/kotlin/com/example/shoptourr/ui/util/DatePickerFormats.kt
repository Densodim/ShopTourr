package com.example.shoptourr.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

object DatePickerFormats {
    fun isoToEpochMillis(iso: String): Long? =
        parseUserDate(iso)?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

    fun epochMillisToIso(millis: Long): String {
        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
        return date.toString()
    }

    fun isValidIsoDate(iso: String): Boolean = iso.isNotBlank() && isoToEpochMillis(iso) != null

    /**
     * Accepts the ISO value the API stores (`2026-08-22`) and the day-first
     * shapes people actually type (`22.08.2026`, `22/08/2026`).
     */
    fun parseUserDate(raw: String): LocalDate? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        runCatching { LocalDate.parse(trimmed) }.getOrNull()?.let { return it }
        val match = DOTTED_OR_SLASH.matchEntire(trimmed) ?: return null
        val day = match.groupValues[1].padStart(2, '0')
        val month = match.groupValues[2].padStart(2, '0')
        val year = match.groupValues[3]
        return runCatching { LocalDate.parse("$year-$month-$day") }.getOrNull()
    }

    /** ISO `yyyy-MM-dd`, or null when [raw] is empty or not a date yet. */
    fun normalizeUserDate(raw: String): String? = parseUserDate(raw)?.toString()

    fun filterDateInput(raw: String): String =
        raw.filter { it.isDigit() || it == '.' || it == '/' || it == '-' }.take(DATE_INPUT_MAX)
}

private val DOTTED_OR_SLASH = Regex("""^(\d{1,2})[./](\d{1,2})[./](\d{4})$""")
private const val DATE_INPUT_MAX = 10

/**
 * A server timestamp as a person reads it: `2026-08-19T10:54:38.031291Z` →
 * `19.08.2026`. Falls back to the raw string when the shape is unfamiliar, so a
 * format change upstream degrades to today's behaviour rather than to blank.
 */
fun formatIsoDay(raw: String): String {
    val date = runCatching { Instant.parse(raw).toLocalDateTime(TimeZone.currentSystemDefault()).date }
        .recoverCatching { LocalDate.parse(raw.substringBefore('T')) }
        .getOrNull()
        ?: return raw
    // LocalDate.toString() is ISO by contract — reversing its parts beats reading
    // the day/month accessors, which have churned across kotlinx-datetime releases.
    val (year, month, day) = date.toString().split('-')
    return "$day.$month.$year"
}
