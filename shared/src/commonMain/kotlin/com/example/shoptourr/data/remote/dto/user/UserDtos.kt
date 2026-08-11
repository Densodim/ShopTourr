package com.example.shoptourr.data.remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
enum class ThemePreference { SYSTEM, LIGHT, DARK }

@Serializable
enum class PremiumPlan { FREE, PLUS, PRO }

@Serializable
data class UserStatsDto(
    val tripsCount: Int,
    val countriesCount: Int,
    val wishlistCount: Int,
)

@Serializable
data class UserDto(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val locale: String,
    val preferredCurrency: String,
    val theme: ThemePreference,
    val pushNotificationsEnabled: Boolean,
    val memberSince: String,
    val premiumPlan: PremiumPlan = PremiumPlan.FREE,
    val stats: UserStatsDto,
)

@Serializable
data class ActivatePremiumRequest(
    val plan: PremiumPlan,
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String,
    val avatarMediaId: String? = null,
)

@Serializable
data class UserPreferencesDto(
    val locale: String,
    val preferredCurrency: String,
    val theme: ThemePreference,
    val pushNotificationsEnabled: Boolean,
    val darkMode: Boolean,
)

@Serializable
data class UpdatePreferencesRequest(
    val locale: String? = null,
    val preferredCurrency: String? = null,
    val theme: ThemePreference? = null,
    val pushNotificationsEnabled: Boolean? = null,
    val darkMode: Boolean? = null,
)
