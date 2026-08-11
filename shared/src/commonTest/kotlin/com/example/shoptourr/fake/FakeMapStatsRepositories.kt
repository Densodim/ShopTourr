package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.repository.RouteRepository
import com.example.shoptourr.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRouteRepository(
    initial: TripRoute? = null,
    private val refreshError: Throwable? = null,
) : RouteRepository {
    private val state = MutableStateFlow(initial)
    var refreshCalls: Int = 0
        private set

    fun setRoute(route: TripRoute?) {
        state.value = route
    }

    override fun observeRoute(tripId: String): Flow<TripRoute?> =
        state.map { it?.takeIf { route -> route.tripId == tripId } }

    override suspend fun refresh(tripId: String): Result<TripRoute> {
        refreshCalls += 1
        refreshError?.let { return Result.failure(it) }
        val current = state.value?.takeIf { it.tripId == tripId }
            ?: return Result.failure(AppError.NotFound)
        return Result.success(current)
    }
}

class FakeStatsRepository(
    initial: TripStats? = null,
    private val refreshError: Throwable? = null,
) : StatsRepository {
    private val state = MutableStateFlow(initial)
    var refreshCalls: Int = 0
        private set

    fun setStats(stats: TripStats?) {
        state.value = stats
    }

    override fun observeStats(tripId: String): Flow<TripStats?> =
        state.map { it?.takeIf { stats -> stats.tripId == tripId } }

    override suspend fun refresh(tripId: String): Result<TripStats> {
        refreshCalls += 1
        refreshError?.let { return Result.failure(it) }
        val current = state.value?.takeIf { it.tripId == tripId }
            ?: return Result.failure(AppError.NotFound)
        return Result.success(current)
    }
}
