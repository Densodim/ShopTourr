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
import com.example.shoptourr.domain.validation.FieldRules
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
        val name = draft.name.trim()
        if (!FieldRules.isTravelerName(name)) return Result.failure(AppError.Validation("name"))
        if (!FieldRules.isHexColor(draft.colorHex)) return Result.failure(AppError.Validation("colorHex"))
        val glyph = draft.avatarGlyph?.trim()?.takeIf { it.isNotEmpty() }
        if (glyph != null && (glyph.length !in 1..2 || !glyph.all { it.isLetter() })) {
            return Result.failure(AppError.Validation("avatarGlyph"))
        }
        return tripRepository.addTraveler(tripId, draft.copy(name = name, avatarGlyph = glyph))
    }
}

class InviteTravelerUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: String, email: String): Result<TripInvite> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        val trimmed = email.trim()
        if (!FieldRules.isEmail(trimmed)) return Result.failure(AppError.Validation("email"))
        return tripRepository.inviteTraveler(tripId, trimmed)
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
