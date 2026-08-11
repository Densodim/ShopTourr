package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseSplitCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseSplitCalculatorTest {

    @Test
    fun `single participant keeps full amount`() {
        val amount = Money.parse("32.50", "EUR")
        assertEquals(amount, PurchaseSplitCalculator.share(amount, participantCount = 1))
    }

    @Test
    fun `splits evenly across travelers`() {
        val amount = Money.parse("32.50", "EUR")
        // 3250 / 3 = 1083 rem 1 → first share gets +1 via Money.splitEqually
        assertEquals(Money(minorUnits = 1084, currency = "EUR"), PurchaseSplitCalculator.share(amount, 3))
    }

    @Test
    fun `zero or negative participants falls back to full amount`() {
        val amount = Money.parse("10.00", "EUR")
        assertEquals(amount, PurchaseSplitCalculator.share(amount, 0))
        assertEquals(amount, PurchaseSplitCalculator.share(amount, -1))
    }
}
