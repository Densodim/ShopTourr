package com.example.shoptourr.data

import com.example.shoptourr.data.local.TripLocalExtrasCodec
import com.example.shoptourr.domain.model.ExchangeRate
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TripLocalExtrasCodecTest {

    @Test
    fun `roundtrips local extras without wire dtos`() {
        val trip = TripSummary(
            id = "lisbon",
            city = "Lisbon",
            country = "Portugal",
            status = TripStatus.ACTIVE,
            startDate = "2026-04-12",
            endDate = "2026-04-19",
            budget = Money.parse("1200.00", "EUR"),
            spent = Money.zero("EUR"),
            purchaseCount = 0,
            exchangeRate = ExchangeRate(
                tripCurrency = "EUR",
                quoteCurrency = "USD",
                rate = "1.08",
                rateDate = "2026-04-12",
                provider = "ecb",
            ),
            travelers = listOf(
                Traveler("me", "Mila", "#FFD84D", "M", isOwner = true),
            ),
        )
        val encoded = TripLocalExtrasCodec.encode(trip)
        assertNotNull(encoded)
        val (rate, travelers) = TripLocalExtrasCodec.decode(encoded)
        assertEquals("1.08", rate?.rate)
        assertEquals("Mila", travelers.single().name)
        assertNull(TripLocalExtrasCodec.encode(trip.copy(exchangeRate = null, travelers = emptyList())))
    }
}
