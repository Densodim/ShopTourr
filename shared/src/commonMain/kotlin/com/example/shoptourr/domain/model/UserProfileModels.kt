package com.example.shoptourr.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class UserStats(
    val tripsCount: Int,
    val countriesCount: Int,
    val wishlistCount: Int,
)

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val locale: String,
    val preferredCurrency: String,
    val theme: ThemeMode,
    val pushNotificationsEnabled: Boolean,
    val memberSince: String,
    val stats: UserStats,
)

data class UserPreferences(
    val locale: String,
    val preferredCurrency: String,
    val theme: ThemeMode,
    val pushNotificationsEnabled: Boolean,
    val darkMode: Boolean,
)

data class UpdateProfileDraft(
    val displayName: String,
)

data class UpdatePreferencesDraft(
    val locale: String? = null,
    val preferredCurrency: String? = null,
    val theme: ThemeMode? = null,
    val pushNotificationsEnabled: Boolean? = null,
    val darkMode: Boolean? = null,
)
