package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserRepository(
    profile: UserProfile? = null,
    preferences: UserPreferences? = null,
    private val refreshError: Throwable? = null,
    private val updateError: Throwable? = null,
) : UserRepository {
    private val profileState = MutableStateFlow(profile)
    private val prefsState = MutableStateFlow(preferences)

    var updateProfileCalls: Int = 0
        private set
    var updatePreferencesCalls: Int = 0
        private set
    var refreshProfileCalls: Int = 0
        private set
    var activatePremiumCalls: Int = 0
        private set

    override fun observeProfile(): Flow<UserProfile?> = profileState.asStateFlow()

    override fun observePreferences(): Flow<UserPreferences?> = prefsState.asStateFlow()

    override suspend fun refreshProfile(): Result<UserProfile> {
        refreshProfileCalls += 1
        refreshError?.let { return Result.failure(it) }
        return profileState.value?.let { Result.success(it) }
            ?: Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
    }

    override suspend fun updateProfile(draft: UpdateProfileDraft): Result<UserProfile> {
        updateError?.let { return Result.failure(it) }
        updateProfileCalls += 1
        val current = profileState.value
            ?: return Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
        val updated = current.copy(displayName = draft.displayName)
        profileState.value = updated
        return Result.success(updated)
    }

    override suspend fun refreshPreferences(): Result<UserPreferences> {
        refreshError?.let { return Result.failure(it) }
        return prefsState.value?.let { Result.success(it) }
            ?: Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
    }

    override suspend fun updatePreferences(draft: UpdatePreferencesDraft): Result<UserPreferences> {
        updateError?.let { return Result.failure(it) }
        updatePreferencesCalls += 1
        val current = prefsState.value
            ?: return Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
        val updated = current.copy(
            locale = draft.locale ?: current.locale,
            preferredCurrency = draft.preferredCurrency ?: current.preferredCurrency,
            theme = draft.theme ?: current.theme,
            pushNotificationsEnabled = draft.pushNotificationsEnabled
                ?: current.pushNotificationsEnabled,
            darkMode = draft.darkMode ?: current.darkMode,
        )
        prefsState.value = updated
        return Result.success(updated)
    }

    override suspend fun activatePremium(plan: PremiumPlan): Result<UserProfile> {
        activatePremiumCalls += 1
        updateError?.let { return Result.failure(it) }
        val current = profileState.value
            ?: return Result.failure(com.example.shoptourr.domain.error.AppError.NotFound)
        val updated = current.copy(premiumPlan = plan)
        profileState.value = updated
        return Result.success(updated)
    }
}
