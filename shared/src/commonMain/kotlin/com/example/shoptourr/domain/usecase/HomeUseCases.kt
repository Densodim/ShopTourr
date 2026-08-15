package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.TripRepository
import com.example.shoptourr.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class ObserveHomeUseCase(
    private val authRepository: AuthRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
) {
    /**
     * Gated on the stored session rather than the in-memory user cache: that cache
     * is empty after a process restart, and keying off it blanked out trips the
     * device already had. The name comes from the persisted profile for the same
     * reason, falling back to the cache right after sign-in, before the profile
     * has been fetched.
     */
    operator fun invoke(): Flow<HomeSnapshot> {
        if (!authRepository.isLoggedIn()) return flowOf(EMPTY)
        return combine(
            tripRepository.observeHome(),
            userRepository.observeProfile(),
        ) { snapshot, profile ->
            val name = profile?.displayName?.takeIf { it.isNotBlank() }
                ?: authRepository.currentUser()?.displayName.orEmpty()
            snapshot.copy(userName = name)
        }
    }

    private companion object {
        val EMPTY = HomeSnapshot(
            userName = "",
            currentTripCity = null,
            upcomingCount = 0,
            archiveCount = 0,
        )
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
