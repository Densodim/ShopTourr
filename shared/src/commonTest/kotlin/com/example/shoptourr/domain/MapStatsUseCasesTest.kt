package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.usecase.RefreshRouteUseCase
import com.example.shoptourr.domain.usecase.RefreshStatsUseCase
import com.example.shoptourr.fake.FakeRouteRepository
import com.example.shoptourr.fake.FakeStatsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class MapStatsUseCasesTest {

    @Test
    fun `refresh route rejects blank trip id`() = runTest {
        assertEquals(
            AppError.Validation("tripId"),
            RefreshRouteUseCase(FakeRouteRepository()).invoke(" ").exceptionOrNull(),
        )
    }

    @Test
    fun `refresh route returns cached route`() = runTest {
        val route = TripRoute(
            tripId = "lisbon",
            stopCount = 1,
            stops = listOf(
                RouteStop(id = "s1", title = "Belem", orderIndex = 0, point = GeoPoint("38.7", "-9.2")),
            ),
        )
        val repo = FakeRouteRepository(route)
        val result = RefreshRouteUseCase(repo)("lisbon").getOrThrow()
        assertEquals(1, result.stopCount)
        assertEquals(1, repo.refreshCalls)
    }

    @Test
    fun `refresh stats rejects blank trip id`() = runTest {
        assertEquals(
            AppError.Validation("tripId"),
            RefreshStatsUseCase(FakeStatsRepository()).invoke("").exceptionOrNull(),
        )
    }

    @Test
    fun `refresh stats returns summary`() = runTest {
        val stats = TripStats(
            tripId = "lisbon",
            totalSpent = Money.parse("100.00", "EUR"),
            budget = Money.parse("1000.00", "EUR"),
            dailyAverage = Money.parse("20.00", "EUR"),
            remaining = Money.parse("900.00", "EUR"),
            onBudget = true,
            topCategory = PurchaseCategory.FOOD,
            byCategory = emptyList(),
            byDay = emptyList(),
        )
        val result = RefreshStatsUseCase(FakeStatsRepository(stats))("lisbon").getOrThrow()
        assertEquals(true, result.onBudget)
        assertEquals(PurchaseCategory.FOOD, result.topCategory)
    }
}
