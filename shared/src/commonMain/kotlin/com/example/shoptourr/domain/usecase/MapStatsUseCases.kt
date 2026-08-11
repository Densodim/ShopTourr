package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.repository.RouteRepository
import com.example.shoptourr.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow

class ObserveRouteUseCase(
    private val routeRepository: RouteRepository,
) {
    operator fun invoke(tripId: String): Flow<TripRoute?> = routeRepository.observeRoute(tripId)
}

class RefreshRouteUseCase(
    private val routeRepository: RouteRepository,
) {
    suspend operator fun invoke(tripId: String): Result<TripRoute> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return routeRepository.refresh(tripId)
    }
}

class ObserveStatsUseCase(
    private val statsRepository: StatsRepository,
) {
    operator fun invoke(tripId: String): Flow<TripStats?> = statsRepository.observeStats(tripId)
}

class RefreshStatsUseCase(
    private val statsRepository: StatsRepository,
) {
    suspend operator fun invoke(tripId: String): Result<TripStats> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return statsRepository.refresh(tripId)
    }
}
