package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTripRepository(
    initial: HomeSnapshot = HomeSnapshot("", null, 0, 0),
) : TripRepository {
    private val home = MutableStateFlow(initial)
    private var queuedRefresh: HomeSnapshot? = null

    var snapshot: HomeSnapshot
        get() = home.value
        set(value) {
            home.value = value
        }

    var refreshError: Throwable? = null

    fun queueRefresh(next: HomeSnapshot) {
        queuedRefresh = next
    }

    override suspend fun refreshTrips(): Result<Unit> {
        refreshError?.let { return Result.failure(it) }
        queuedRefresh?.let {
            home.value = it
            queuedRefresh = null
        }
        return Result.success(Unit)
    }

    override fun observeHome(): Flow<HomeSnapshot> = home.asStateFlow()
}
