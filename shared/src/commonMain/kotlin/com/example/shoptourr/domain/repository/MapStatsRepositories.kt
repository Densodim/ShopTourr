package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun observeRoute(tripId: String): Flow<TripRoute?>
    suspend fun refresh(tripId: String): Result<TripRoute>
}

interface StatsRepository {
    fun observeStats(tripId: String): Flow<TripStats?>
    suspend fun refresh(tripId: String): Result<TripStats>
}
