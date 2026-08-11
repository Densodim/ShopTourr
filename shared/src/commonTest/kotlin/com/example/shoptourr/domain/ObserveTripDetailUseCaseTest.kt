package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeTripRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ObserveTripDetailUseCaseTest {

    @Test
    fun `emits trip with purchases for matching id`() = runTest {
        val trip = TripSummary(
            id = "lisbon",
            city = "Lisbon",
            country = "Portugal",
            status = TripStatus.ACTIVE,
            startDate = "2026-08-01",
            endDate = "2026-08-10",
            budget = Money.parse("1000.00", "EUR"),
            spent = Money.zero("EUR"),
            purchaseCount = 0,
        )
        val trips = FakeTripRepository(
            initial = HomeSnapshot("Mila", "Lisbon", 0, 0, "lisbon"),
            trips = listOf(trip),
        )
        val purchases = FakePurchaseRepository()
        purchases.create(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = "Belem",
            ),
        )

        val detail = ObserveTripDetailUseCase(trips, purchases)("lisbon").first()

        assertNotNull(detail)
        assertEquals("Lisbon", detail.trip.city)
        assertEquals(1, detail.purchases.size)
        assertEquals("Pasteis", detail.purchases.first().name)
    }

    @Test
    fun `emits null when trip missing`() = runTest {
        val detail = ObserveTripDetailUseCase(
            FakeTripRepository(),
            FakePurchaseRepository(),
        )("missing").first()
        assertNull(detail)
    }
}
