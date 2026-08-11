package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.ExchangeRate
import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripInvite
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.repository.TripRepository
import com.example.shoptourr.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RefreshTripUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: String): Result<TripSummary> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return tripRepository.refreshTrip(tripId)
    }
}

class AddTravelerUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: String, draft: CreateTravelerDraft): Result<Traveler> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        if (draft.name.isBlank()) return Result.failure(AppError.Validation("name"))
        return tripRepository.addTraveler(tripId, draft.copy(name = draft.name.trim()))
    }
}

class InviteTravelerUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: String, email: String): Result<TripInvite> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        if (email.isBlank() || !email.contains("@")) return Result.failure(AppError.Validation("email"))
        return tripRepository.inviteTraveler(tripId, email.trim())
    }
}

class RefreshExchangeRateUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: String): Result<ExchangeRate> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return tripRepository.refreshExchangeRate(tripId)
    }
}

class ActivatePremiumUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(plan: PremiumPlan): Result<UserProfile> {
        if (plan == PremiumPlan.FREE) return Result.failure(AppError.Validation("plan"))
        return userRepository.activatePremium(plan)
    }
}

class ObservePremiumUseCase(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        userRepository.observeProfile().map { it?.isPremium == true }
}
