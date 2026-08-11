package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveHomeUseCase(
    private val authRepository: AuthRepository,
    private val tripRepository: TripRepository,
) {
    operator fun invoke(): Flow<HomeSnapshot> {
        val user = authRepository.currentUser()
            ?: return flowOf(HomeSnapshot(userName = "", currentTripCity = null, upcomingCount = 0, archiveCount = 0))
        return tripRepository.observeHome().map { snapshot ->
            snapshot.copy(userName = user.displayName)
        }
    }
}

class RefreshHomeUseCase(
    private val tripRepository: TripRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(): Result<Unit> {
        drainSyncOutbox?.invoke()
        return tripRepository.refreshTrips()
    }
}
