package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseSearch
import com.example.shoptourr.domain.model.VatCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseSearchTest {

    @Test
    fun `filters by name or place without requiring fts`() {
        val pasteis = purchase("Pasteis de nata", "Belem")
        val tram = purchase("Tram 28", "Alfama")
        val found = PurchaseSearch.filter(listOf(pasteis, tram), "belem")
        assertEquals(listOf(pasteis), found)
        assertEquals(listOf(tram), PurchaseSearch.filter(listOf(pasteis, tram), "tram"))
        assertEquals(listOf(pasteis, tram), PurchaseSearch.filter(listOf(pasteis, tram), " "))
    }

    private fun purchase(name: String, place: String): Purchase {
        val amount = Money.parse("1.00", "EUR")
        return Purchase(
            id = name,
            tripId = "lisbon",
            name = name,
            category = PurchaseCategory.FOOD,
            amount = amount,
            vat = VatCalculator.breakdown(amount, "23", true),
            taxRefundEligible = false,
            place = place,
            purchaseDate = "2026-08-01",
            purchaseTime = null,
            pendingSync = false,
        )
    }
}
