package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchasePageKeyset
import com.example.shoptourr.domain.model.PurchasePageRequest
import com.example.shoptourr.domain.model.VatCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchasePageKeysetTest {

    private fun purchase(id: String, date: String): Purchase {
        val amount = Money.parse("1.00", "EUR")
        return Purchase(
            id = id,
            tripId = "lisbon",
            name = id,
            category = PurchaseCategory.FOOD,
            amount = amount,
            vat = VatCalculator.breakdown(amount, "23", true),
            taxRefundEligible = false,
            place = null,
            purchaseDate = date,
            purchaseTime = null,
            pendingSync = false,
        )
    }

    @Test
    fun `offset page takes size after skip`() {
        val items = listOf(
            purchase("c", "2026-08-13"),
            purchase("b", "2026-08-12"),
            purchase("a", "2026-08-11"),
        )
        val page = PurchasePageKeyset.slice(items, PurchasePageRequest(page = 1, size = 1))
        assertEquals(listOf("b"), page.map { it.id })
    }

    @Test
    fun `keyset after date and id returns older rows`() {
        val items = listOf(
            purchase("c", "2026-08-13"),
            purchase("b", "2026-08-12"),
            purchase("a", "2026-08-11"),
        )
        val page = PurchasePageKeyset.slice(
            items,
            PurchasePageRequest(size = 10, afterDate = "2026-08-13", afterId = "c"),
        )
        assertEquals(listOf("b", "a"), page.map { it.id })
    }
}
