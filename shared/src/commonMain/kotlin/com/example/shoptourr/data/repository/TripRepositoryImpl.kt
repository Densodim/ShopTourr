package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.dto.common.ExchangeRateDto
import com.example.shoptourr.data.remote.dto.trip.CreateTravelerRequest
import com.example.shoptourr.data.remote.dto.trip.InviteTravelerRequest
import com.example.shoptourr.data.remote.dto.trip.TravelerDto
import com.example.shoptourr.data.remote.dto.trip.TripDto
import com.example.shoptourr.data.remote.dto.trip.TripInviteDto
import com.example.shoptourr.data.remote.dto.trip.TripInviteStatus as ApiTripInviteStatus
import com.example.shoptourr.data.remote.dto.trip.TripSummaryDto
import com.example.shoptourr.data.remote.dto.trip.TripStatus as ApiTripStatus
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.data.sync.CreateTripPayload
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncPayloadCodec
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
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val homeApi: HomeApi,
    private val tripApi: TripApi,
    private val localStore: TripLocalStore,
    private val outbox: SyncOutbox,
    private val idGenerator: () -> String,
    private val clock: () -> Long,
) : TripRepository {

    override suspend fun refreshTrips(): Result<Unit> =
        runCatching {
            val home = homeApi.fetchHome()
            val trips = buildList {
                home.currentTrip?.let { add(it.toDomain()) }
                addAll(home.upcoming.map { it.toDomain() })
                addAll(home.archive.map { it.toDomain() })
            }
            localStore.replaceAll(trips)
        }.mapHttpAppError()

    override suspend fun refreshTrip(tripId: String): Result<TripSummary> =
        runCatching {
            val trip = tripApi.fetchTrip(tripId).toDomain()
            localStore.upsert(trip)
            trip
        }.mapHttpAppError()

    override suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary> =
        runCatching {
            val localId = idGenerator()
            val now = clock()
            val travelers = draft.travelers.mapIndexed { index, item ->
                Traveler(
                    id = "$localId-t$index",
                    name = item.name,
                    colorHex = item.colorHex,
                    avatarGlyph = item.avatarGlyph ?: item.name.take(1).uppercase(),
                    isOwner = index == 0,
                )
            }
            val trip = TripSummary(
                id = localId,
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
            localStore.upsert(trip)
            val payload = CreateTripPayload(
                localId = localId,
                city = draft.city,
                country = draft.country,
                countryCode = draft.countryCode,
                startDate = draft.startDate,
                endDate = draft.endDate,
                budgetAmount = draft.budget.toDecimalString(),
                budgetCurrency = draft.budget.currency,
                defaultVatRatePercent = draft.defaultVatRatePercent,
                quoteCurrency = draft.quoteCurrency,
                travelers = draft.travelers.map {
                    CreateTripPayload.Traveler(it.name, it.colorHex, it.avatarGlyph)
                },
            )
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-trip-$localId",
                    type = SyncMutationType.CREATE_TRIP,
                    payloadJson = SyncPayloadCodec.encodeTrip(payload),
                    idempotencyKey = localId,
                    createdAtEpochMs = now,
                ),
            )
            trip
        }.mapHttpAppError()

    override suspend fun addTraveler(tripId: String, draft: CreateTravelerDraft): Result<Traveler> =
        runCatching {
            val traveler = tripApi.addTraveler(
                tripId = tripId,
                request = CreateTravelerRequest(
                    name = draft.name,
                    colorHex = draft.colorHex,
                    avatarGlyph = draft.avatarGlyph,
                ),
            ).toDomain()
            val current = localStore.all().firstOrNull { it.id == tripId }
            if (current != null) {
                localStore.upsert(current.copy(travelers = current.travelers + traveler))
            }
            traveler
        }.mapHttpAppError()

    override suspend fun inviteTraveler(tripId: String, email: String): Result<TripInvite> =
        runCatching {
            tripApi.inviteTraveler(
                tripId = tripId,
                request = InviteTravelerRequest(email = email),
            ).toDomain()
        }.mapHttpAppError()

    override suspend fun refreshExchangeRate(tripId: String): Result<ExchangeRate> =
        runCatching {
            val rate = tripApi.refreshExchangeRate(tripId).toDomain()
            val current = localStore.all().firstOrNull { it.id == tripId }
            if (current != null) {
                localStore.upsert(current.copy(exchangeRate = rate))
            }
            rate
        }.mapHttpAppError()

    override fun observeHome(): Flow<HomeSnapshot> =
        localStore.observeAll().map { trips ->
            TripSummary.toHomeSnapshot(userName = "", trips = trips)
        }

    override fun observeTrip(tripId: String): Flow<TripSummary?> =
        localStore.observeAll().map { trips -> trips.firstOrNull { it.id == tripId } }

    private fun TripSummaryDto.toDomain(): TripSummary =
        TripSummary(
            id = id,
            city = city,
            country = country,
            status = status.toDomain(),
            startDate = startDate,
            endDate = endDate,
            budget = Money.parse(budget.amount, budget.currency),
            spent = Money.parse(spent.amount, spent.currency),
            purchaseCount = purchaseCount,
            flagEmoji = flagEmoji,
            datesLabel = datesLabel,
            currentDayNumber = currentDayNumber,
            dayCount = dayCount,
        )

    private fun TripDto.toDomain(): TripSummary =
        TripSummary(
            id = id,
            city = city,
            country = country,
            status = status.toDomain(),
            startDate = startDate,
            endDate = endDate,
            budget = Money.parse(budget.amount, budget.currency),
            spent = Money.parse(spent.amount, spent.currency),
            purchaseCount = purchaseCount,
            flagEmoji = flagEmoji,
            datesLabel = datesLabel,
            currentDayNumber = currentDayNumber,
            dayCount = dayCount,
            exchangeRate = exchangeRate?.toDomain(),
            travelers = travelers.map { it.toDomain() },
        )

    private fun TravelerDto.toDomain(): Traveler =
        Traveler(
            id = id,
            name = name,
            colorHex = colorHex,
            avatarGlyph = avatarGlyph,
            isOwner = isOwner,
        )

    private fun ExchangeRateDto.toDomain(): ExchangeRate =
        ExchangeRate(
            tripCurrency = tripCurrency,
            quoteCurrency = quoteCurrency,
            rate = rate,
            rateDate = rateDate,
            provider = provider,
        )

    private fun TripInviteDto.toDomain(): TripInvite =
        TripInvite(
            id = id,
            tripId = tripId,
            email = email,
            status = when (status) {
                ApiTripInviteStatus.PENDING -> TripInviteStatus.PENDING
                ApiTripInviteStatus.ACCEPTED -> TripInviteStatus.ACCEPTED
                ApiTripInviteStatus.DECLINED -> TripInviteStatus.DECLINED
                ApiTripInviteStatus.EXPIRED -> TripInviteStatus.EXPIRED
            },
            createdAt = createdAt,
            expiresAt = expiresAt,
        )

    private fun ApiTripStatus.toDomain(): TripStatus = when (this) {
        ApiTripStatus.UPCOMING -> TripStatus.UPCOMING
        ApiTripStatus.ACTIVE -> TripStatus.ACTIVE
        ApiTripStatus.PAST -> TripStatus.PAST
        ApiTripStatus.ARCHIVED -> TripStatus.ARCHIVED
    }
}
