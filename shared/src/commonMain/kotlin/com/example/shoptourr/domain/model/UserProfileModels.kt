package com.example.shoptourr.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class PremiumPlan {
    FREE,
    PLUS,
    PRO,
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
    val premiumPlan: PremiumPlan = PremiumPlan.FREE,
    val stats: UserStats,
) {
    val isPremium: Boolean get() = premiumPlan != PremiumPlan.FREE
}

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
