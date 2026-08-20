package com.example.shoptourr.domain.widget

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BudgetWidgetCopyTest {

    private lateinit var previous: AppLocale

    @BeforeTest
    fun setUp() {
        previous = VoyageI18n.currentLocale
        VoyageI18n.currentLocale = AppLocale.EN
    }

    @AfterTest
    fun tearDown() {
        VoyageI18n.currentLocale = previous
    }

    @Test
    fun `no current trip shows the empty copy`() {
        val snapshot = BudgetWidgetCopy.of(HomeSnapshot("", null, 0, 0))
        assertNull(snapshot.tripId)
        assertEquals("No trip", snapshot.city)
        assertEquals("Remaining budget shows on a trip", snapshot.remainingLine)
        assertFalse(snapshot.overBudget)
    }

    @Test
    fun `active trip shows city and remaining`() {
        val snapshot = BudgetWidgetCopy.of(
            HomeSnapshot(
                userName = "Mila",
                currentTripCity = "Lisbon",
                upcomingCount = 0,
                archiveCount = 0,
                currentTripId = "lisbon",
                currentTrip = lisbon(spent = "120.00", budget = "1000.00"),
            ),
        )
        assertEquals("lisbon", snapshot.tripId)
        assertEquals("Lisbon", snapshot.city)
        assertEquals("Left 880.00 EUR", snapshot.remainingLine)
        assertFalse(snapshot.overBudget)
    }

    @Test
    fun `overspend replaces remaining with over-by`() {
        val snapshot = BudgetWidgetCopy.of(
            HomeSnapshot(
                userName = "Mila",
                currentTripCity = "Lisbon",
                upcomingCount = 0,
                archiveCount = 0,
                currentTripId = "lisbon",
                currentTrip = lisbon(spent = "1100.00", budget = "1000.00"),
            ),
        )
        assertEquals("Over by 100.00 EUR", snapshot.remainingLine)
        assertTrue(snapshot.overBudget)
    }

    @Test
    fun `round-trips through json for the widget process`() {
        val original = BudgetWidgetCopy.of(
            HomeSnapshot(
                userName = "Mila",
                currentTripCity = "Lisbon",
                upcomingCount = 0,
                archiveCount = 0,
                currentTripId = "lisbon",
                currentTrip = lisbon(spent = "120.00", budget = "1000.00"),
            ),
        )
        val restored = BudgetWidgetCopy.decode(BudgetWidgetCopy.encode(original))
        assertEquals(original, restored)
    }

    private fun lisbon(spent: String, budget: String) = TripSummary(
        id = "lisbon",
        city = "Lisbon",
        country = "Portugal",
        status = TripStatus.ACTIVE,
        startDate = "2026-08-01",
        endDate = "2026-08-10",
        budget = Money.parse(budget, "EUR"),
        spent = Money.parse(spent, "EUR"),
        purchaseCount = 1,
    )
}
