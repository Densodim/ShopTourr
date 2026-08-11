package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.RouteLocalStore
import com.example.shoptourr.data.local.StatsLocalStore
import com.example.shoptourr.data.remote.RouteApi
import com.example.shoptourr.data.remote.StatsApi
import com.example.shoptourr.data.remote.dto.map.GeoPointDto
import com.example.shoptourr.data.remote.dto.map.RouteStopDto
import com.example.shoptourr.data.remote.dto.map.TripRouteDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory as ApiPurchaseCategory
import com.example.shoptourr.data.remote.dto.stats.CategorySpendDto
import com.example.shoptourr.data.remote.dto.stats.DailySpendDto
import com.example.shoptourr.data.remote.dto.stats.TripStatsDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.CategorySpend
import com.example.shoptourr.domain.model.DailySpend
import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.repository.RouteRepository
import com.example.shoptourr.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow

class RouteRepositoryImpl(
    private val api: RouteApi,
    private val localStore: RouteLocalStore,
) : RouteRepository {
    override fun observeRoute(tripId: String): Flow<TripRoute?> = localStore.observe(tripId)

    override suspend fun refresh(tripId: String): Result<TripRoute> =
        runCatching {
            val route = api.fetchRoute(tripId).toDomain()
            localStore.save(route)
            route
        }.mapHttpAppError()
}

class StatsRepositoryImpl(
    private val api: StatsApi,
    private val localStore: StatsLocalStore,
) : StatsRepository {
    override fun observeStats(tripId: String): Flow<TripStats?> = localStore.observe(tripId)

    override suspend fun refresh(tripId: String): Result<TripStats> =
        runCatching {
            val stats = api.fetchStats(tripId).toDomain()
            localStore.save(stats)
            stats
        }.mapHttpAppError()
}

private fun TripRouteDto.toDomain(): TripRoute =
    TripRoute(
        tripId = tripId,
        stopCount = stopCount,
        distanceMeters = distanceMeters,
        stops = stops.map { it.toDomain() },
        path = path.map { it.toDomain() },
    )

private fun RouteStopDto.toDomain(): RouteStop =
    RouteStop(
        id = id,
        title = title,
        place = place,
        date = date,
        amountSpentHere = amountSpentHere?.let { Money.parse(it.amount, it.currency) },
        point = point?.toDomain(),
        orderIndex = orderIndex,
    )

private fun GeoPointDto.toDomain(): GeoPoint = GeoPoint(lat = lat, lng = lng)

private fun TripStatsDto.toDomain(): TripStats =
    TripStats(
        tripId = tripId,
        totalSpent = Money.parse(totalSpent.amount, totalSpent.currency),
        budget = Money.parse(budget.amount, budget.currency),
        dailyAverage = Money.parse(dailyAverage.amount, dailyAverage.currency),
        remaining = Money.parse(remaining.amount, remaining.currency),
        onBudget = onBudget,
        paceDeltaDays = paceDeltaDays,
        topCategory = topCategory?.toDomain(),
        byCategory = byCategory.map { it.toDomain() },
        byDay = byDay.map { it.toDomain() },
    )

private fun CategorySpendDto.toDomain(): CategorySpend =
    CategorySpend(
        category = category.toDomain(),
        amount = Money.parse(amount.amount, amount.currency),
        share = share,
        purchaseCount = purchaseCount,
    )

private fun DailySpendDto.toDomain(): DailySpend =
    DailySpend(
        date = date,
        amount = Money.parse(amount.amount, amount.currency),
        purchaseCount = purchaseCount,
    )

private fun ApiPurchaseCategory.toDomain(): PurchaseCategory = when (this) {
    ApiPurchaseCategory.FOOD -> PurchaseCategory.FOOD
    ApiPurchaseCategory.TRANSPORT -> PurchaseCategory.TRANSPORT
    ApiPurchaseCategory.SOUVENIRS -> PurchaseCategory.SOUVENIRS
    ApiPurchaseCategory.HOTEL -> PurchaseCategory.HOTEL
    ApiPurchaseCategory.CULTURE -> PurchaseCategory.CULTURE
    ApiPurchaseCategory.OTHER -> PurchaseCategory.OTHER
}
