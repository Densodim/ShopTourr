package com.example.shoptourr.data.repository

import com.example.shoptourr.data.remote.dto.trip.TripSummaryDto
import com.example.shoptourr.data.remote.dto.trip.TripStatus as ApiTripStatus
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.sync.CreateTripPayload
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncPayloadCodec
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
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

    override suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary> =
        runCatching {
            val localId = idGenerator()
            val now = clock()
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
            )
            localStore.replaceAll(localStore.all() + trip)
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
            )
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-trip-$localId",
                    type = SyncMutationType.CREATE_TRIP,
                    payloadJson = SyncPayloadCodec.encodeTrip(payload),
                    idempotencyKey = localId,
                    createdAtEpochMs = now,
                )
            )
            trip
        }.mapHttpAppError()

    override fun observeHome(): Flow<HomeSnapshot> =
        localStore.observeAll().map { trips ->
            TripSummary.toHomeSnapshot(userName = "", trips = trips)
        }

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

    private fun ApiTripStatus.toDomain(): TripStatus = when (this) {
        ApiTripStatus.UPCOMING -> TripStatus.UPCOMING
        ApiTripStatus.ACTIVE -> TripStatus.ACTIVE
        ApiTripStatus.PAST -> TripStatus.PAST
        ApiTripStatus.ARCHIVED -> TripStatus.ARCHIVED
    }
}
