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
