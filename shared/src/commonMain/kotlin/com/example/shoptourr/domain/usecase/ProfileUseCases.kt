package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = userRepository.observeProfile()
}

class ObservePreferencesUseCase(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<UserPreferences?> = userRepository.observePreferences()
}

class RefreshProfileUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<UserProfile> = userRepository.refreshProfile()
}

class RefreshPreferencesUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<UserPreferences> = userRepository.refreshPreferences()
}

class UpdateProfileUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(draft: UpdateProfileDraft): Result<UserProfile> {
        if (draft.displayName.trim().isEmpty()) {
            return Result.failure(AppError.Validation("displayName"))
        }
        return userRepository.updateProfile(draft.copy(displayName = draft.displayName.trim()))
    }
}

class UpdatePreferencesUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(draft: UpdatePreferencesDraft): Result<UserPreferences> {
        if (draft.locale != null && draft.locale.isBlank()) {
            return Result.failure(AppError.Validation("locale"))
        }
        if (draft.preferredCurrency != null && draft.preferredCurrency.length != 3) {
            return Result.failure(AppError.Validation("preferredCurrency"))
        }
        return userRepository.updatePreferences(draft)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(allSessions: Boolean = false): Result<Unit> =
        authRepository.logout(allSessions)
}
