package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.ExchangeRate
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripInvite
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.UpdateTripDraft
import com.example.shoptourr.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String, deviceName: String? = null): Result<AuthSession>
    suspend fun loginSocial(
        provider: SocialProvider,
        idToken: String,
        nonce: String?,
        displayName: String? = null,
        deviceName: String? = null,
    ): Result<AuthSession>
    suspend fun register(
        displayName: String,
        email: String,
        password: String,
        locale: String = "ru",
    ): Result<AuthSession>
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun resetPassword(email: String, token: String, newPassword: String): Result<Unit>
    suspend fun refresh(): Result<AuthSession>
    suspend fun logout(allSessions: Boolean = false): Result<Unit>
    fun currentUser(): User?
    fun isLoggedIn(): Boolean
}

interface TripRepository {
    suspend fun refreshTrips(): Result<Unit>
    suspend fun refreshTrip(tripId: String): Result<TripSummary>
    suspend fun createTrip(draft: CreateTripDraft): Result<TripSummary>
    suspend fun updateTrip(tripId: String, draft: UpdateTripDraft): Result<TripSummary>
    suspend fun deleteTrip(tripId: String): Result<Unit>
    suspend fun addTraveler(tripId: String, draft: CreateTravelerDraft): Result<Traveler>
    suspend fun inviteTraveler(tripId: String, email: String): Result<TripInvite>
    suspend fun refreshExchangeRate(tripId: String): Result<ExchangeRate>
    fun observeHome(): Flow<HomeSnapshot>
    fun observeTrip(tripId: String): Flow<TripSummary?>
}
