package com.example.shoptourr.domain.widget

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.PublishBudgetWidgetUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class PublishBudgetWidgetUseCaseTest {

    private lateinit var previous: AppLocale

    @BeforeTest
    fun setUp() {
        previous = VoyageI18n.currentLocale
        VoyageI18n.currentLocale = AppLocale.EN
    }

    @AfterTest
    fun tearDown() {
        VoyageI18n.currentLocale = previous
    }

    @Test
    fun `writes remaining for the current trip and asks the launcher to reload`() = runTest {
        val store = RecordingBudgetWidgetStore()
        val refresher = RecordingBudgetWidgetRefresher()
        PublishBudgetWidgetUseCase(
            observeHome = homeUseCase(
                HomeSnapshot(
                    userName = "Mila",
                    currentTripCity = "Lisbon",
                    upcomingCount = 0,
                    archiveCount = 0,
                    currentTripId = "lisbon",
                    currentTrip = TripSummary(
                        id = "lisbon",
                        city = "Lisbon",
                        country = "Portugal",
                        status = TripStatus.ACTIVE,
                        startDate = "2026-08-01",
                        endDate = "2026-08-10",
                        budget = Money.parse("1000.00", "EUR"),
                        spent = Money.parse("120.00", "EUR"),
                        purchaseCount = 1,
                    ),
                ),
            ),
            store = store,
            refresher = refresher,
        ).publish()

        assertEquals("Lisbon", store.last?.city)
        assertEquals("Left 880.00 EUR", store.last?.remainingLine)
        assertEquals(1, refresher.reloads)
    }

    @Test
    fun `empty home still publishes so the widget does not keep a stale trip`() = runTest {
        val store = RecordingBudgetWidgetStore()
        PublishBudgetWidgetUseCase(
            observeHome = homeUseCase(HomeSnapshot("", null, 0, 0)),
            store = store,
            refresher = RecordingBudgetWidgetRefresher(),
        ).publish()
        assertEquals("No trip", store.last?.city)
    }

    private fun homeUseCase(home: HomeSnapshot) = ObserveHomeUseCase(
        authRepository = FakeAuthRepository(loggedInOverride = true),
        tripRepository = FakeTripRepository(home),
        userRepository = FakeUserRepository(),
    )

    private class RecordingBudgetWidgetStore : BudgetWidgetStore {
        var last: BudgetWidgetSnapshot? = null
        override fun write(snapshot: BudgetWidgetSnapshot) {
            last = snapshot
        }

        override fun read(): BudgetWidgetSnapshot? = last
    }

    private class RecordingBudgetWidgetRefresher : BudgetWidgetRefresher {
        var reloads: Int = 0
        override fun reload() {
            reloads += 1
        }
    }
}
