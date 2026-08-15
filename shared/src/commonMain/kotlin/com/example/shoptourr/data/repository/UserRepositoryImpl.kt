package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.UserLocalStore
import com.example.shoptourr.data.remote.UserApi
import com.example.shoptourr.data.remote.dto.user.ActivatePremiumRequest
import com.example.shoptourr.data.remote.dto.user.PremiumPlan as ApiPremiumPlan
import com.example.shoptourr.data.remote.dto.user.ThemePreference
import com.example.shoptourr.data.remote.dto.user.UpdatePreferencesRequest
import com.example.shoptourr.data.remote.dto.user.UpdateProfileRequest
import com.example.shoptourr.data.remote.dto.user.UserDto
import com.example.shoptourr.data.remote.dto.user.UserPreferencesDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val api: UserApi,
    private val localStore: UserLocalStore,
) : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> = localStore.observeProfile()

    override fun observePreferences(): Flow<UserPreferences?> = localStore.observePreferences()

    override suspend fun refreshProfile(): Result<UserProfile> =
        runCatching {
            val profile = api.fetchMe().toDomain()
            localStore.saveProfile(profile)
            profile
        }.mapHttpAppError()

    override suspend fun updateProfile(draft: UpdateProfileDraft): Result<UserProfile> =
        runCatching {
            val profile = api.updateMe(UpdateProfileRequest(displayName = draft.displayName)).toDomain()
            localStore.saveProfile(profile)
            profile
        }.mapHttpAppError()

    override suspend fun refreshPreferences(): Result<UserPreferences> =
        runCatching {
            val prefs = api.fetchPreferences().toDomain()
            localStore.savePreferences(prefs)
            prefs
        }.mapHttpAppError()

    override suspend fun updatePreferences(draft: UpdatePreferencesDraft): Result<UserPreferences> =
        runCatching {
            val prefs = api.updatePreferences(
                UpdatePreferencesRequest(
                    locale = draft.locale,
                    preferredCurrency = draft.preferredCurrency,
                    theme = draft.theme?.toDto(),
                    pushNotificationsEnabled = draft.pushNotificationsEnabled,
                    darkMode = draft.darkMode,
                )
            ).toDomain()
            localStore.savePreferences(prefs)
            prefs
        }.mapHttpAppError()

    override suspend fun activatePremium(plan: PremiumPlan): Result<UserProfile> =
        runCatching {
            val profile = api.activatePremium(
                ActivatePremiumRequest(plan = plan.toDto()),
            ).toDomain()
            localStore.saveProfile(profile)
            profile
        }.mapHttpAppError()
}

internal fun UserDto.toDomain(): UserProfile =
    UserProfile(
        id = id,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        locale = locale,
        preferredCurrency = preferredCurrency,
        theme = theme.toDomain(),
        pushNotificationsEnabled = pushNotificationsEnabled,
        memberSince = memberSince,
        premiumPlan = premiumPlan.toDomain(),
        stats = UserStats(
            tripsCount = stats.tripsCount,
            countriesCount = stats.countriesCount,
            wishlistCount = stats.wishlistCount,
        ),
    )

private fun UserPreferencesDto.toDomain(): UserPreferences =
    UserPreferences(
        locale = locale,
        preferredCurrency = preferredCurrency,
        theme = theme.toDomain(),
        pushNotificationsEnabled = pushNotificationsEnabled,
        darkMode = darkMode,
    )

private fun ThemePreference.toDomain(): ThemeMode = when (this) {
    ThemePreference.SYSTEM -> ThemeMode.SYSTEM
    ThemePreference.LIGHT -> ThemeMode.LIGHT
    ThemePreference.DARK -> ThemeMode.DARK
}

private fun ThemeMode.toDto(): ThemePreference = when (this) {
    ThemeMode.SYSTEM -> ThemePreference.SYSTEM
    ThemeMode.LIGHT -> ThemePreference.LIGHT
    ThemeMode.DARK -> ThemePreference.DARK
}

private fun ApiPremiumPlan.toDomain(): PremiumPlan = when (this) {
    ApiPremiumPlan.FREE -> PremiumPlan.FREE
    ApiPremiumPlan.PLUS -> PremiumPlan.PLUS
    ApiPremiumPlan.PRO -> PremiumPlan.PRO
}

private fun PremiumPlan.toDto(): ApiPremiumPlan = when (this) {
    PremiumPlan.FREE -> ApiPremiumPlan.FREE
    PremiumPlan.PLUS -> ApiPremiumPlan.PLUS
    PremiumPlan.PRO -> ApiPremiumPlan.PRO
}
