package com.example.shoptourr.ui

import com.example.shoptourr.ui.util.TripDayLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class TripDayLabelTest {

    private val today = LocalDate.parse("2026-08-15")

    @Test
    fun `names today and yesterday by catalog key`() {
        assertEquals("today", TripDayLabel.keyFor("2026-08-15", today))
        assertEquals("yesterday", TripDayLabel.keyFor("2026-08-14", today))
    }

    @Test
    fun `older days fall back to the raw date`() {
        assertNull(TripDayLabel.keyFor("2026-08-13", today))
    }

    @Test
    fun `future days are not called today`() {
        assertNull(TripDayLabel.keyFor("2026-08-16", today))
    }

    @Test
    fun `an unparseable date does not blow up the list`() {
        assertNull(TripDayLabel.keyFor("not-a-date", today))
    }

    @Test
    fun `crossing a month boundary still resolves yesterday`() {
        assertEquals(
            "yesterday",
            TripDayLabel.keyFor("2026-07-31", LocalDate.parse("2026-08-01")),
        )
    }
}
