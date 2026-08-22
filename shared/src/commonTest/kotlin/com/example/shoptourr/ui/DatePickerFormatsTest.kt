package com.example.shoptourr.ui

import com.example.shoptourr.ui.util.DatePickerFormats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DatePickerFormatsTest {

    @Test
    fun `iso dates round-trip`() {
        val millis = DatePickerFormats.isoToEpochMillis("2026-08-22")
        assertNotNull(millis)
        assertEquals("2026-08-22", DatePickerFormats.epochMillisToIso(millis))
        assertEquals("2026-08-22", DatePickerFormats.normalizeUserDate("2026-08-22"))
    }

    @Test
    fun `day-first dotted and slashed dates become iso`() {
        assertEquals("2026-08-22", DatePickerFormats.normalizeUserDate("22.08.2026"))
        assertEquals("2026-08-22", DatePickerFormats.normalizeUserDate("22/08/2026"))
        assertEquals("2026-08-02", DatePickerFormats.normalizeUserDate("2.8.2026"))
    }

    @Test
    fun `partial or impossible input stays unresolved`() {
        assertNull(DatePickerFormats.normalizeUserDate(""))
        assertNull(DatePickerFormats.normalizeUserDate("22.08"))
        assertNull(DatePickerFormats.normalizeUserDate("2026-13-40"))
        assertNull(DatePickerFormats.normalizeUserDate("32.08.2026"))
    }

    @Test
    fun `filter keeps date characters and caps length`() {
        assertEquals("22.08.2026", DatePickerFormats.filterDateInput("22.08.2026abc"))
        assertEquals("2026-08-22", DatePickerFormats.filterDateInput("2026-08-22extra"))
    }
}
