package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface RouteLocalStore {
    fun observe(tripId: String): Flow<TripRoute?>
    suspend fun save(route: TripRoute)
}

class InMemoryRouteLocalStore : RouteLocalStore {
    private val byTrip = MutableStateFlow<Map<String, TripRoute>>(emptyMap())

    override fun observe(tripId: String): Flow<TripRoute?> = byTrip.map { it[tripId] }

    override suspend fun save(route: TripRoute) {
        byTrip.value = byTrip.value + (route.tripId to route)
    }
}

interface StatsLocalStore {
    fun observe(tripId: String): Flow<TripStats?>
    suspend fun save(stats: TripStats)
}

class InMemoryStatsLocalStore : StatsLocalStore {
    private val byTrip = MutableStateFlow<Map<String, TripStats>>(emptyMap())

    override fun observe(tripId: String): Flow<TripStats?> = byTrip.map { it[tripId] }

    override suspend fun save(stats: TripStats) {
        byTrip.value = byTrip.value + (stats.tripId to stats)
    }
}
