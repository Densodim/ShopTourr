package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.VatBreakdown
import com.example.shoptourr.domain.model.VatCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VatCalculatorTest {

    @Test
    fun `vat included splits gross into net and vat`() {
        val gross = Money.parse("23.00", "EUR")
        val result = VatCalculator.breakdown(gross, vatRatePercent = "23", vatIncluded = true)

        assertEquals(Money.parse("18.70", "EUR"), result.net)
        assertEquals(Money.parse("4.30", "EUR"), result.vat)
        assertEquals(Money.parse("23.00", "EUR"), result.gross)
        assertEquals(true, result.vatIncluded)
    }

    @Test
    fun `vat excluded adds vat on top of net`() {
        val net = Money.parse("100.00", "EUR")
        val result = VatCalculator.breakdown(net, vatRatePercent = "23", vatIncluded = false)

        assertEquals(Money.parse("100.00", "EUR"), result.net)
        assertEquals(Money.parse("23.00", "EUR"), result.vat)
        assertEquals(Money.parse("123.00", "EUR"), result.gross)
        assertEquals(false, result.vatIncluded)
    }

    @Test
    fun `zero vat rate keeps amount unchanged`() {
        val amount = Money.parse("10.00", "EUR")
        val included = VatCalculator.breakdown(amount, "0", vatIncluded = true)
        assertEquals(amount, included.net)
        assertEquals(Money.zero("EUR"), included.vat)

        val excluded = VatCalculator.breakdown(amount, "0", vatIncluded = false)
        assertEquals(amount, excluded.gross)
        assertEquals(Money.zero("EUR"), excluded.vat)
    }

    @Test
    fun `rejects negative vat rate`() {
        assertFailsWith<IllegalArgumentException> {
            VatCalculator.breakdown(Money.parse("10.00", "EUR"), "-1", true)
        }
    }
}

class MoneyTest {

    @Test
    fun `parses and formats two-decimal currency`() {
        val money = Money.parse("1 234.50", "EUR")
        assertEquals("1234.50", money.toDecimalString())
        assertEquals("EUR", money.currency)
    }

    @Test
    fun `adds same currency amounts`() {
        val sum = Money.parse("10.25", "EUR") + Money.parse("0.75", "EUR")
        assertEquals(Money.parse("11.00", "EUR"), sum)
    }

    @Test
    fun `split equally rounds remainder to first share`() {
        val shares = Money.parse("10.00", "EUR").splitEqually(3)
        assertEquals(3, shares.size)
        assertEquals(Money.parse("10.00", "EUR"), shares.reduce(Money::plus))
        assertEquals(Money.parse("3.34", "EUR"), shares[0])
        assertEquals(Money.parse("3.33", "EUR"), shares[1])
        assertEquals(Money.parse("3.33", "EUR"), shares[2])
    }

    @Test
    fun `rejects currency mismatch on add`() {
        assertFailsWith<IllegalArgumentException> {
            Money.parse("1.00", "EUR") + Money.parse("1.00", "USD")
        }
    }
}
