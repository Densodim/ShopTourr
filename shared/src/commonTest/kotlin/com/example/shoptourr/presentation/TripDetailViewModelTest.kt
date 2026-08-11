package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.usecase.AddTravelerUseCase
import com.example.shoptourr.domain.usecase.InviteTravelerUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.RefreshExchangeRateUseCase
import com.example.shoptourr.domain.usecase.RefreshTripUseCase
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

    private fun tripRepo(trip: TripSummary) = FakeTripRepository(
        initial = HomeSnapshot("Mila", trip.city, 0, 0, trip.id),
        trips = listOf(trip),
    )

    private fun vm(trips: FakeTripRepository, purchases: FakePurchaseRepository = FakePurchaseRepository()) =
        TripDetailViewModel(
            tripId = "lisbon",
            observeTripDetail = ObserveTripDetailUseCase(trips, purchases),
            refreshTrip = RefreshTripUseCase(trips),
            addTraveler = AddTravelerUseCase(trips),
            inviteTraveler = InviteTravelerUseCase(trips),
            refreshExchangeRate = RefreshExchangeRateUseCase(trips),
        )

    private val sampleTrip = TripSummary(
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

    @Test
    fun `loads trip detail and purchases`() = runTest {
        val trips = tripRepo(sampleTrip)
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
        val viewModel = vm(trips, purchases)

        viewModel.state.test {
            var state = awaitItem()
            if (state.detail == null) state = awaitItem()
            assertEquals("Lisbon", state.detail?.trip?.city)
            assertEquals(1, state.detail?.purchases?.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onCleared()
    }

    @Test
    fun `add purchase intent emits navigation event`() = runTest {
        val viewModel = vm(tripRepo(sampleTrip))
        viewModel.events.test {
            viewModel.onIntent(TripDetailIntent.AddPurchase)
            assertIs<TripDetailUiEvent.NavigateAddPurchase>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onCleared()
    }

    @Test
    fun `invite traveler stores last invite`() = runTest {
        val trips = tripRepo(sampleTrip)
        val viewModel = vm(trips)
        viewModel.onIntent(TripDetailIntent.InviteEmailChanged("friend@voyage.app"))
        viewModel.onIntent(TripDetailIntent.InviteTraveler)
        assertEquals("friend@voyage.app", viewModel.state.value.lastInvite?.email)
        assertEquals(1, trips.inviteCalls)
        viewModel.onCleared()
    }
}
