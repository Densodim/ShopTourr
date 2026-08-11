package com.example.shoptourr.presentation

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.usecase.ObserveRouteUseCase
import com.example.shoptourr.domain.usecase.ObserveStatsUseCase
import com.example.shoptourr.domain.usecase.RefreshRouteUseCase
import com.example.shoptourr.domain.usecase.RefreshStatsUseCase
import com.example.shoptourr.fake.FakeRouteRepository
import com.example.shoptourr.fake.FakeStatsRepository
import com.example.shoptourr.presentation.map.RouteIntent
import com.example.shoptourr.presentation.map.RouteViewModel
import com.example.shoptourr.presentation.stats.StatsIntent
import com.example.shoptourr.presentation.stats.StatsViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MapStatsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `route view model loads stops`() = runTest {
        val route = TripRoute(
            tripId = "lisbon",
            stopCount = 1,
            stops = listOf(
                RouteStop(
                    id = "s1",
                    title = "Belem",
                    orderIndex = 0,
                    point = GeoPoint("38.7", "-9.2"),
                ),
            ),
        )
        val vm = RouteViewModel(
            tripId = "lisbon",
            observeRoute = ObserveRouteUseCase(FakeRouteRepository(route)),
            refreshRoute = RefreshRouteUseCase(FakeRouteRepository(route)),
        )
        assertEquals("Belem", vm.state.value.route?.stops?.first()?.title)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun `route refresh failure maps UiError`() = runTest {
        val vm = RouteViewModel(
            tripId = "lisbon",
            observeRoute = ObserveRouteUseCase(FakeRouteRepository()),
            refreshRoute = RefreshRouteUseCase(
                FakeRouteRepository(refreshError = AppError.Unauthorized),
            ),
        )
        vm.onIntent(RouteIntent.Refresh)
        assertEquals("Сессия истекла", vm.state.value.error?.title)
        vm.onCleared()
    }

    @Test
    fun `stats view model loads totals`() = runTest {
        val stats = TripStats(
            tripId = "lisbon",
            totalSpent = Money.parse("120.00", "EUR"),
            budget = Money.parse("1000.00", "EUR"),
            dailyAverage = Money.parse("30.00", "EUR"),
            remaining = Money.parse("880.00", "EUR"),
            onBudget = true,
            topCategory = PurchaseCategory.FOOD,
            byCategory = emptyList(),
            byDay = emptyList(),
        )
        val repo = FakeStatsRepository(stats)
        val vm = StatsViewModel(
            tripId = "lisbon",
            observeStats = ObserveStatsUseCase(repo),
            refreshStats = RefreshStatsUseCase(repo),
        )
        assertEquals("120.00", vm.state.value.stats?.totalSpent?.toDecimalString())
        vm.onIntent(StatsIntent.Refresh)
        assertEquals(2, repo.refreshCalls)
        vm.onCleared()
    }
}
