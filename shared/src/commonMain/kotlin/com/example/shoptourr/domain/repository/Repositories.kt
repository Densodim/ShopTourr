package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String, deviceName: String? = null): Result<AuthSession>
    suspend fun register(
        displayName: String,
        email: String,
        password: String,
        locale: String = "ru",
    ): Result<AuthSession>
    suspend fun refresh(): Result<AuthSession>
    suspend fun logout(allSessions: Boolean = false): Result<Unit>
    fun currentUser(): User?
    fun isLoggedIn(): Boolean
}

interface TripRepository {
    suspend fun refreshTrips(): Result<Unit>
    suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary>
    fun observeHome(): Flow<HomeSnapshot>
    fun observeTrip(tripId: String): Flow<TripSummary?>
}
