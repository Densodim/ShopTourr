package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTripRepository(
    initial: HomeSnapshot = HomeSnapshot("", null, 0, 0),
    private val createError: Throwable? = null,
    trips: List<TripSummary> = emptyList(),
) : TripRepository {
    private val home = MutableStateFlow(initial)
    private val tripList = MutableStateFlow(trips)
    private var queuedRefresh: HomeSnapshot? = null

    var snapshot: HomeSnapshot
        get() = home.value
        set(value) {
            home.value = value
        }

    var refreshError: Throwable? = null
    var createCalls: Int = 0
        private set

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

    override suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary> {
        createError?.let { return Result.failure(it) }
        createCalls += 1
        val trip = TripSummary(
            id = "trip-$createCalls",
            city = draft.city,
            country = draft.country,
            status = TripStatus.UPCOMING,
            startDate = draft.startDate,
            endDate = draft.endDate,
            budget = draft.budget,
            spent = Money.zero(draft.budget.currency),
            purchaseCount = 0,
        )
        tripList.update { it + trip }
        home.value = home.value.copy(upcomingCount = home.value.upcomingCount + 1)
        return Result.success(trip)
    }

    override fun observeHome(): Flow<HomeSnapshot> = home.asStateFlow()

    override fun observeTrip(tripId: String): Flow<TripSummary?> =
        tripList.map { list -> list.firstOrNull { it.id == tripId } }
}
