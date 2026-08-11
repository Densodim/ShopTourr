package com.example.shoptourr.api.user

enum class ThemePreference { SYSTEM, LIGHT, DARK }

data class UserStatsDto(
    val tripsCount: Int,
    val countriesCount: Int,
    val wishlistCount: Int,
)

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
    val stats: UserStatsDto,
)

data class UpdateProfileRequest(
    val displayName: String,
    val avatarMediaId: String? = null,
)

data class UserPreferencesDto(
    val locale: String,
    val preferredCurrency: String,
    val theme: ThemePreference,
    val pushNotificationsEnabled: Boolean,
    val darkMode: Boolean,
)

data class UpdatePreferencesRequest(
    val locale: String? = null,
    val preferredCurrency: String? = null,
    val theme: ThemePreference? = null,
    val pushNotificationsEnabled: Boolean? = null,
    val darkMode: Boolean? = null,
)
