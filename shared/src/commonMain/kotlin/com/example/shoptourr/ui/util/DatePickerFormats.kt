package com.example.shoptourr.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

object DatePickerFormats {
    fun isoToEpochMillis(iso: String): Long? =
        runCatching { LocalDate.parse(iso).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()

    fun epochMillisToIso(millis: Long): String {
        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
        return date.toString()
    }

    fun isValidIsoDate(iso: String): Boolean = iso.isNotBlank() && isoToEpochMillis(iso) != null
}

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
