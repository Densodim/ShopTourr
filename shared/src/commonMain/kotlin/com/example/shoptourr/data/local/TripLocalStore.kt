package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.TripSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TripLocalStore {
    suspend fun replaceAll(trips: List<TripSummary>)
    fun observeAll(): Flow<List<TripSummary>>
    fun all(): List<TripSummary>
}

class InMemoryTripLocalStore : TripLocalStore {
    private val trips = MutableStateFlow<List<TripSummary>>(emptyList())

    override suspend fun replaceAll(trips: List<TripSummary>) {
        this.trips.value = trips
    }

    override fun observeAll(): Flow<List<TripSummary>> = trips.asStateFlow()

    override fun all(): List<TripSummary> = trips.value
}
