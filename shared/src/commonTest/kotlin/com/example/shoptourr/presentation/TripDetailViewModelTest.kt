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
import com.example.shoptourr.domain.usecase.RefreshPurchasesUseCase
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
import kotlin.test.assertTrue
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

    private fun vm(
        trips: FakeTripRepository,
        purchases: FakePurchaseRepository = FakePurchaseRepository(),
        pageSize: Int = 50,
    ) =
        TripDetailViewModel(
            tripId = "lisbon",
            observeTripDetail = ObserveTripDetailUseCase(trips, purchases),
            refreshTrip = RefreshTripUseCase(trips),
            addTraveler = AddTravelerUseCase(trips),
            inviteTraveler = InviteTravelerUseCase(trips),
            refreshExchangeRate = RefreshExchangeRateUseCase(trips),
            refreshPurchases = RefreshPurchasesUseCase(purchases),
            purchasePageSize = pageSize,
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
    fun `category filter narrows the listed days and toggles off when reselected`() = runTest {
        val trips = tripRepo(sampleTrip)
        val purchases = FakePurchaseRepository()
        purchases.create("lisbon", draft("Pasteis", PurchaseCategory.FOOD, "4.50"))
        purchases.create("lisbon", draft("Tram", PurchaseCategory.TRANSPORT, "3.00"))
        val viewModel = vm(trips, purchases)

        viewModel.state.test {
            var state = awaitItem()
            if (state.detail == null) state = awaitItem()
            assertNull(state.categoryFilter)
            assertEquals(2, state.visibleDays.sumOf { it.items.size })

            viewModel.onIntent(TripDetailIntent.CategoryFilterChanged(PurchaseCategory.FOOD))
            state = awaitItem()
            assertEquals(PurchaseCategory.FOOD, state.categoryFilter)
            assertEquals(1, state.visibleDays.sumOf { it.items.size })
            assertEquals("4.50", state.visibleDays.first().total.toDecimalString())

            // Tapping the active chip clears the filter rather than reapplying it.
            viewModel.onIntent(TripDetailIntent.CategoryFilterChanged(PurchaseCategory.FOOD))
            state = awaitItem()
            assertNull(state.categoryFilter)
            assertEquals(2, state.visibleDays.sumOf { it.items.size })

            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onCleared()
    }

    @Test
    fun `prefetch requests the next keyset page after the first fetch`() = runTest {
        val trips = tripRepo(sampleTrip)
        val purchases = FakePurchaseRepository()
        purchases.create("lisbon", dated("One", "2026-08-15"))
        purchases.create("lisbon", dated("Two", "2026-08-14"))
        purchases.create("lisbon", dated("Three", "2026-08-13"))
        purchases.create("lisbon", dated("Four", "2026-08-12"))
        purchases.create("lisbon", dated("Five", "2026-08-11"))
        val viewModel = vm(trips, purchases, pageSize = 2)

        viewModel.state.test {
            var state = awaitItem()
            while (state.isLoading || purchases.refreshPageCalls < 2) {
                state = awaitItem()
            }
            assertEquals(2, purchases.refreshPageCalls)
            assertEquals(null, purchases.refreshRequests[0].afterId)
            assertEquals("p-2", purchases.refreshRequests[1].afterId)
            assertEquals("2026-08-14", purchases.refreshRequests[1].afterDate)
            assertTrue(state.hasMorePurchases)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onCleared()
    }

    @Test
    fun `load more continues from the prefetched keyset cursor`() = runTest {
        val trips = tripRepo(sampleTrip)
        val purchases = FakePurchaseRepository()
        purchases.create("lisbon", dated("One", "2026-08-15"))
        purchases.create("lisbon", dated("Two", "2026-08-14"))
        purchases.create("lisbon", dated("Three", "2026-08-13"))
        purchases.create("lisbon", dated("Four", "2026-08-12"))
        purchases.create("lisbon", dated("Five", "2026-08-11"))
        val viewModel = vm(trips, purchases, pageSize = 2)

        viewModel.state.test {
            var state = awaitItem()
            while (state.isLoading || purchases.refreshPageCalls < 2) {
                state = awaitItem()
            }
            viewModel.onIntent(TripDetailIntent.LoadMore)
            state = awaitItem()
            while (state.isLoadingMore || purchases.refreshPageCalls < 3) {
                state = awaitItem()
            }
            assertEquals(3, purchases.refreshPageCalls)
            assertEquals("p-4", purchases.lastRefreshRequest?.afterId)
            assertTrue(!state.hasMorePurchases)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onCleared()
    }

    private fun draft(name: String, category: PurchaseCategory, amount: String) = PurchaseDraft(
        name = name,
        category = category,
        amount = Money.parse(amount, "EUR"),
        vatIncluded = true,
        vatRatePercent = "23",
        place = null,
    )

    private fun dated(name: String, date: String) = PurchaseDraft(
        name = name,
        category = PurchaseCategory.FOOD,
        amount = Money.parse("1.00", "EUR"),
        vatIncluded = true,
        vatRatePercent = "23",
        place = null,
        purchaseDate = date,
    )

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
    fun `empty traveler name maps to the name field`() = runTest {
        val viewModel = vm(tripRepo(sampleTrip))
        viewModel.onIntent(TripDetailIntent.AddTraveler)
        assertEquals(null, viewModel.state.value.error)
        assertEquals("validation_name_required", viewModel.state.value.fieldErrors.travelerName)
        viewModel.onCleared()
    }

    @Test
    fun `invalid invite email maps to the email field`() = runTest {
        val viewModel = vm(tripRepo(sampleTrip))
        viewModel.onIntent(TripDetailIntent.InviteEmailChanged("not-an-email"))
        viewModel.onIntent(TripDetailIntent.InviteTraveler)
        assertEquals(null, viewModel.state.value.error)
        assertEquals("validation_email_invalid", viewModel.state.value.fieldErrors.inviteEmail)
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

    @Test
    fun `share sends a trip card to the share sheet`() = runTest {
        val trips = tripRepo(sampleTrip)
        val purchases = FakePurchaseRepository()
        purchases.create("lisbon", draft("Pasteis", PurchaseCategory.FOOD, "4.50"))
        val sent = mutableListOf<String>()
        val viewModel = TripDetailViewModel(
            tripId = "lisbon",
            observeTripDetail = ObserveTripDetailUseCase(trips, purchases),
            refreshTrip = RefreshTripUseCase(trips),
            addTraveler = AddTravelerUseCase(trips),
            inviteTraveler = InviteTravelerUseCase(trips),
            refreshExchangeRate = RefreshExchangeRateUseCase(trips),
            refreshPurchases = RefreshPurchasesUseCase(purchases),
            shareSheet = { text -> sent += text },
        )

        viewModel.state.test {
            var state = awaitItem()
            if (state.detail == null) state = awaitItem()
            viewModel.onIntent(TripDetailIntent.Share)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, sent.size)
        assertTrue(sent.first().contains("Lisbon, Portugal"))
        assertTrue(sent.first().contains("4.50"))
        viewModel.onCleared()
    }
}
