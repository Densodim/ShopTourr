package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.LocalSessionStore
import com.example.shoptourr.domain.repository.UserRepository
import com.example.shoptourr.domain.session.AuthTokenCache
import com.example.shoptourr.domain.session.NoOpAuthTokenCache
import com.example.shoptourr.domain.validation.FieldRules
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
        val displayName = draft.displayName.trim()
        if (!FieldRules.isPersonName(displayName)) {
            return Result.failure(AppError.Validation("displayName"))
        }
        return userRepository.updateProfile(draft.copy(displayName = displayName))
    }
}

class UpdatePreferencesUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(draft: UpdatePreferencesDraft): Result<UserPreferences> {
        val normalizedLocale = draft.locale?.let { raw ->
            val tag = raw.trim().lowercase()
            when {
                tag == "ru" || tag.startsWith("ru-") -> "ru"
                tag == "en" || tag.startsWith("en-") -> "en"
                else -> return Result.failure(AppError.Validation("locale"))
            }
        }
        if (draft.preferredCurrency != null && !FieldRules.isSupportedCurrency(draft.preferredCurrency)) {
            return Result.failure(AppError.Validation("preferredCurrency"))
        }
        return userRepository.updatePreferences(draft.copy(locale = normalizedLocale))
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val localSessionStore: LocalSessionStore? = null,
    private val unregisterPushDevice: UnregisterPushDeviceUseCase? = null,
    private val authTokenCache: AuthTokenCache = NoOpAuthTokenCache,
) {
    suspend operator fun invoke(allSessions: Boolean = false): Result<Unit> {
        unregisterPushDevice?.invoke()
        authRepository.logout(allSessions)
        authTokenCache.clear()
        localSessionStore?.clearUserData()
        return Result.success(Unit)
    }
}

class DeleteAccountUseCase(
    private val userRepository: UserRepository,
    private val logout: LogoutUseCase,
) {
    suspend operator fun invoke(): Result<Unit> {
        val deleted = userRepository.deleteAccount()
        if (deleted.isSuccess) {
            logout()
        }
        return deleted
    }
}
