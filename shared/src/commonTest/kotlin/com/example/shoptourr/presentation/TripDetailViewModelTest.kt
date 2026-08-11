package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.presentation.trip.TripDetailIntent
import com.example.shoptourr.presentation.trip.TripDetailUiEvent
import com.example.shoptourr.presentation.trip.TripDetailViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TripDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads trip detail and purchases`() = runTest {
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
            "lisbon",
            PurchaseDraft(
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = null,
            ),
        )
        val vm = TripDetailViewModel(
            tripId = "lisbon",
            observeTripDetail = ObserveTripDetailUseCase(trips, purchases),
        )

        vm.state.test {
            var state = awaitItem()
            if (state.detail == null) state = awaitItem()
            assertEquals("Lisbon", state.detail?.trip?.city)
            assertEquals(1, state.detail?.purchases?.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onCleared()
    }

    @Test
    fun `add purchase intent emits navigation event`() = runTest {
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
        val vm = TripDetailViewModel(
            tripId = "lisbon",
            observeTripDetail = ObserveTripDetailUseCase(
                FakeTripRepository(trips = listOf(trip)),
                FakePurchaseRepository(),
            ),
        )

        vm.events.test {
            vm.onIntent(TripDetailIntent.AddPurchase)
            assertIs<TripDetailUiEvent.NavigateAddPurchase>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.onCleared()
    }
}
