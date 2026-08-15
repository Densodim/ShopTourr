package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.VatBreakdown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TripDetailBreakdownTest {

    @Test
    fun `groups purchases by day, newest first, with a per-day total`() {
        val detail = detail(
            purchase("p1", "Кофе", PurchaseCategory.FOOD, "4.50", date = "2026-08-15"),
            purchase("p2", "Трамвай", PurchaseCategory.TRANSPORT, "3.00", date = "2026-08-15"),
            purchase("p3", "Плитка", PurchaseCategory.SOUVENIRS, "24.00", date = "2026-08-14"),
        )

        val days = detail.dayGroups()

        assertEquals(listOf("2026-08-15", "2026-08-14"), days.map { it.date })
        assertEquals("7.50", days[0].total.toDecimalString())
        assertEquals("24.00", days[1].total.toDecimalString())
        assertEquals(2, days[0].items.size)
    }

    @Test
    fun `filters by category and keeps day totals consistent with the filter`() {
        val detail = detail(
            purchase("p1", "Кофе", PurchaseCategory.FOOD, "4.50", date = "2026-08-15"),
            purchase("p2", "Трамвай", PurchaseCategory.TRANSPORT, "3.00", date = "2026-08-15"),
        )

        val days = detail.dayGroups(filter = PurchaseCategory.FOOD)

        assertEquals(1, days.size)
        assertEquals(1, days[0].items.size)
        assertEquals("4.50", days[0].total.toDecimalString())
    }

    @Test
    fun `lists only the categories actually used, in spend order`() {
        val detail = detail(
            purchase("p1", "Кофе", PurchaseCategory.FOOD, "4.50"),
            purchase("p2", "Плитка", PurchaseCategory.SOUVENIRS, "24.00"),
            purchase("p3", "Трамвай", PurchaseCategory.TRANSPORT, "3.00"),
        )

        assertEquals(
            listOf(PurchaseCategory.SOUVENIRS, PurchaseCategory.FOOD, PurchaseCategory.TRANSPORT),
            detail.categoriesUsed(),
        )
    }

    @Test
    fun `totals vat across purchases and refundable vat separately`() {
        val detail = detail(
            purchase("p1", "Кофе", PurchaseCategory.FOOD, "4.50", vat = "0.84"),
            purchase("p2", "Плитка", PurchaseCategory.SOUVENIRS, "24.00", vat = "4.49", refundable = true),
        )

        assertEquals("5.33", detail.vatTotal().toDecimalString())
        assertEquals("4.49", detail.taxRefundTotal().toDecimalString())
    }

    @Test
    fun `spend progress is capped at one hundred percent when over budget`() {
        val detail = detail(
            purchase("p1", "Всё сразу", PurchaseCategory.OTHER, "1500.00"),
            budget = "1200.00",
        )
        assertEquals(100, detail.spendPercent())
        assertTrue(detail.isOverBudget())
    }

    @Test
    fun `remaining goes negative when the budget is blown`() {
        val detail = detail(
            purchase("p1", "Всё сразу", PurchaseCategory.OTHER, "1500.00"),
            budget = "1200.00",
        )
        assertEquals("-300.00", detail.remaining().toDecimalString())
    }

    @Test
    fun `an empty trip reports zero progress rather than dividing by zero`() {
        val detail = detail(budget = "0.00")
        assertEquals(0, detail.spendPercent())
        assertTrue(detail.dayGroups().isEmpty())
    }

    private fun detail(vararg purchases: Purchase, budget: String = "1200.00") = TripDetail(
        trip = TripSummary(
            id = "t1",
            city = "Lisbon",
            country = "Portugal",
            status = TripStatus.ACTIVE,
            startDate = "2026-08-13",
            endDate = "2026-08-20",
            budget = Money.parse(budget, "EUR"),
            spent = Money.zero("EUR"),
            purchaseCount = purchases.size,
        ),
        purchases = purchases.toList(),
    )

    private fun purchase(
        id: String,
        name: String,
        category: PurchaseCategory,
        amount: String,
        date: String = "2026-08-15",
        vat: String = "0.00",
        refundable: Boolean = false,
    ) = Purchase(
        id = id,
        tripId = "t1",
        name = name,
        category = category,
        amount = Money.parse(amount, "EUR"),
        vat = VatBreakdown(
            net = Money.parse(amount, "EUR"),
            vat = Money.parse(vat, "EUR"),
            gross = Money.parse(amount, "EUR"),
            vatRatePercent = "23",
            vatIncluded = true,
        ),
        taxRefundEligible = refundable,
        place = "Baixa",
        purchaseDate = date,
        purchaseTime = "10:24",
        pendingSync = false,
    )
}
