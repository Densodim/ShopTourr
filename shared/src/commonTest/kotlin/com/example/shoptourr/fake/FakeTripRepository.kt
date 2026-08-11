package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.ExchangeRate
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripInvite
import com.example.shoptourr.domain.model.TripInviteStatus
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
    var inviteCalls: Int = 0
        private set
    var fxRefreshCalls: Int = 0
        private set

    fun queueRefresh(next: HomeSnapshot) {
        queuedRefresh = next
    }

    fun setTrips(trips: List<TripSummary>) {
        tripList.value = trips
    }

    override suspend fun refreshTrips(): Result<Unit> {
        refreshError?.let { return Result.failure(it) }
        queuedRefresh?.let {
            home.value = it
            queuedRefresh = null
        }
        return Result.success(Unit)
    }

    override suspend fun refreshTrip(tripId: String): Result<TripSummary> {
        val trip = tripList.value.firstOrNull { it.id == tripId }
            ?: return Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
        return Result.success(trip)
    }

    override suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary> {
        createError?.let { return Result.failure(it) }
        createCalls += 1
        val travelers = draft.travelers.mapIndexed { index, item ->
            Traveler(
                id = "t-$createCalls-$index",
                name = item.name,
                colorHex = item.colorHex,
                avatarGlyph = item.avatarGlyph ?: item.name.take(1),
                isOwner = index == 0,
            )
        }
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
            travelers = travelers,
        )
        tripList.update { it + trip }
        home.value = home.value.copy(upcomingCount = home.value.upcomingCount + 1)
        return Result.success(trip)
    }

    override suspend fun addTraveler(tripId: String, draft: CreateTravelerDraft): Result<Traveler> {
        val traveler = Traveler(
            id = "traveler-${draft.name}",
            name = draft.name,
            colorHex = draft.colorHex,
            avatarGlyph = draft.avatarGlyph ?: draft.name.take(1),
            isOwner = false,
        )
        tripList.update { list ->
            list.map { trip ->
                if (trip.id == tripId) trip.copy(travelers = trip.travelers + traveler) else trip
            }
        }
        return Result.success(traveler)
    }

    override suspend fun inviteTraveler(tripId: String, email: String): Result<TripInvite> {
        inviteCalls += 1
        return Result.success(
            TripInvite(
                id = "invite-$inviteCalls",
                tripId = tripId,
                email = email,
                status = TripInviteStatus.PENDING,
                createdAt = "2026-01-01T00:00:00Z",
            ),
        )
    }

    override suspend fun refreshExchangeRate(tripId: String): Result<ExchangeRate> {
        fxRefreshCalls += 1
        val rate = ExchangeRate(
            tripCurrency = "EUR",
            quoteCurrency = "RUB",
            rate = "98.50",
            rateDate = "2026-01-01",
            provider = "ECB",
        )
        tripList.update { list ->
            list.map { trip ->
                if (trip.id == tripId) trip.copy(exchangeRate = rate) else trip
            }
        }
        return Result.success(rate)
    }

    override fun observeHome(): Flow<HomeSnapshot> = home.asStateFlow()

    override fun observeTrip(tripId: String): Flow<TripSummary?> =
        tripList.map { list -> list.firstOrNull { it.id == tripId } }
}
