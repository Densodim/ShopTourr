package com.example.shoptourr.data.repository

import com.example.shoptourr.api.trip.TripSummaryDto
import com.example.shoptourr.api.trip.TripStatus as ApiTripStatus
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val homeApi: HomeApi,
    private val localStore: TripLocalStore,
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
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                Result.failure(error as? AppError ?: AppError.Unknown(error.message))
            },
        )

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
