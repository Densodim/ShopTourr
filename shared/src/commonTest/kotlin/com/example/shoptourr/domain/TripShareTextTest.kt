package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.share.TripShareText
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripShareTextTest {

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
    fun `formats city dates spend and remaining`() {
        val text = TripShareText.of(detail(spent = "120.00", budget = "1000.00", purchases = 2))
        assertContains(text, "Lisbon, Portugal")
        assertContains(text, "2026-08-01 → 2026-08-10")
        assertContains(text, "120.00 / 1000.00 EUR")
        assertContains(text, "Left 880.00 EUR")
        assertContains(text, "2 purchases")
    }

    @Test
    fun `over budget line replaces remaining`() {
        val text = TripShareText.of(detail(spent = "1100.00", budget = "1000.00", purchases = 1))
        assertContains(text, "Over by 100.00 EUR")
        assertFalse(text.contains("Left"))
        assertTrue(text.contains("1 purchase"))
    }

    private fun detail(spent: String, budget: String, purchases: Int): TripDetail {
        val currency = "EUR"
        val total = Money.parse(spent, currency)
        val perItem = if (purchases <= 1) total else Money(total.minorUnits / purchases, currency)
        val remainder = if (purchases <= 1) 0L else total.minorUnits - perItem.minorUnits * purchases
        val items = List(purchases) { index ->
            val amount = if (index == 0) Money(perItem.minorUnits + remainder, currency) else perItem
            val vat = VatCalculator.breakdown(amount, "0", vatIncluded = true)
            Purchase(
                id = "p-$index",
                tripId = "lisbon",
                name = "Item $index",
                category = PurchaseCategory.FOOD,
                amount = amount,
                vat = vat,
                taxRefundEligible = false,
                place = null,
                purchaseDate = "2026-08-02",
                purchaseTime = null,
                pendingSync = false,
            )
        }
        return TripDetail(
            trip = TripSummary(
                id = "lisbon",
                city = "Lisbon",
                country = "Portugal",
                status = TripStatus.ACTIVE,
                startDate = "2026-08-01",
                endDate = "2026-08-10",
                budget = Money.parse(budget, currency),
                spent = Money.parse(spent, currency),
                purchaseCount = purchases,
            ),
            purchases = items,
        )
    }
}
