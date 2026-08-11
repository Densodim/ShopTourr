package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface UserLocalStore {
    fun observeProfile(): Flow<UserProfile?>
    fun observePreferences(): Flow<UserPreferences?>
    fun profile(): UserProfile?
    fun preferences(): UserPreferences?
    fun saveProfile(profile: UserProfile)
    fun savePreferences(preferences: UserPreferences)
    fun clear()
}

class InMemoryUserLocalStore : UserLocalStore {
    private val profileState = MutableStateFlow<UserProfile?>(null)
    private val prefsState = MutableStateFlow<UserPreferences?>(null)

    override fun observeProfile(): Flow<UserProfile?> = profileState.asStateFlow()
    override fun observePreferences(): Flow<UserPreferences?> = prefsState.asStateFlow()
    override fun profile(): UserProfile? = profileState.value
    override fun preferences(): UserPreferences? = prefsState.value
    override fun saveProfile(profile: UserProfile) {
        profileState.value = profile
    }
    override fun savePreferences(preferences: UserPreferences) {
        prefsState.value = preferences
    }
    override fun clear() {
        profileState.value = null
        prefsState.value = null
    }
}

class SettingsUserLocalStore(
    private val settings: Settings,
) : UserLocalStore {
    private val profileState = MutableStateFlow(readProfile())
    private val prefsState = MutableStateFlow(readPreferences())

    override fun observeProfile(): Flow<UserProfile?> = profileState.asStateFlow()
    override fun observePreferences(): Flow<UserPreferences?> = prefsState.asStateFlow()
    override fun profile(): UserProfile? = profileState.value
    override fun preferences(): UserPreferences? = prefsState.value

    override fun saveProfile(profile: UserProfile) {
        settings[KEY_ID] = profile.id
        settings[KEY_DISPLAY_NAME] = profile.displayName
        settings[KEY_EMAIL] = profile.email
        settings[KEY_AVATAR] = profile.avatarUrl.orEmpty()
        settings[KEY_LOCALE] = profile.locale
        settings[KEY_CURRENCY] = profile.preferredCurrency
        settings[KEY_THEME] = profile.theme.name
        settings[KEY_PUSH] = profile.pushNotificationsEnabled
        settings[KEY_MEMBER_SINCE] = profile.memberSince
        settings[KEY_PREMIUM] = profile.premiumPlan.name
        settings[KEY_TRIPS] = profile.stats.tripsCount
        settings[KEY_COUNTRIES] = profile.stats.countriesCount
        settings[KEY_WISHLIST] = profile.stats.wishlistCount
        profileState.value = profile
    }

    override fun savePreferences(preferences: UserPreferences) {
        settings[KEY_PREF_LOCALE] = preferences.locale
        settings[KEY_PREF_CURRENCY] = preferences.preferredCurrency
        settings[KEY_PREF_THEME] = preferences.theme.name
        settings[KEY_PREF_PUSH] = preferences.pushNotificationsEnabled
        settings[KEY_PREF_DARK] = preferences.darkMode
        prefsState.value = preferences
    }

    override fun clear() {
        listOf(
            KEY_ID, KEY_DISPLAY_NAME, KEY_EMAIL, KEY_AVATAR, KEY_LOCALE, KEY_CURRENCY,
            KEY_THEME, KEY_PUSH, KEY_MEMBER_SINCE, KEY_PREMIUM, KEY_TRIPS, KEY_COUNTRIES, KEY_WISHLIST,
            KEY_PREF_LOCALE, KEY_PREF_CURRENCY, KEY_PREF_THEME, KEY_PREF_PUSH, KEY_PREF_DARK,
        ).forEach { settings.remove(it) }
        profileState.value = null
        prefsState.value = null
    }

    private fun readProfile(): UserProfile? {
        val id = settings.getStringOrNull(KEY_ID) ?: return null
        return UserProfile(
            id = id,
            displayName = settings.getString(KEY_DISPLAY_NAME, ""),
            email = settings.getString(KEY_EMAIL, ""),
            avatarUrl = settings.getStringOrNull(KEY_AVATAR)?.ifBlank { null },
            locale = settings.getString(KEY_LOCALE, "ru"),
            preferredCurrency = settings.getString(KEY_CURRENCY, "EUR"),
            theme = ThemeMode.valueOf(settings.getString(KEY_THEME, ThemeMode.SYSTEM.name)),
            pushNotificationsEnabled = settings.getBoolean(KEY_PUSH, true),
            memberSince = settings.getString(KEY_MEMBER_SINCE, ""),
            premiumPlan = runCatching {
                PremiumPlan.valueOf(settings.getString(KEY_PREMIUM, PremiumPlan.FREE.name))
            }.getOrDefault(PremiumPlan.FREE),
            stats = UserStats(
                tripsCount = settings.getInt(KEY_TRIPS, 0),
                countriesCount = settings.getInt(KEY_COUNTRIES, 0),
                wishlistCount = settings.getInt(KEY_WISHLIST, 0),
            ),
        )
    }

    private fun readPreferences(): UserPreferences? {
        val locale = settings.getStringOrNull(KEY_PREF_LOCALE) ?: return null
        return UserPreferences(
            locale = locale,
            preferredCurrency = settings.getString(KEY_PREF_CURRENCY, "EUR"),
            theme = ThemeMode.valueOf(settings.getString(KEY_PREF_THEME, ThemeMode.SYSTEM.name)),
            pushNotificationsEnabled = settings.getBoolean(KEY_PREF_PUSH, true),
            darkMode = settings.getBoolean(KEY_PREF_DARK, false),
        )
    }

    private companion object {
        const val KEY_ID = "user.profile.id"
        const val KEY_DISPLAY_NAME = "user.profile.display_name"
        const val KEY_EMAIL = "user.profile.email"
        const val KEY_AVATAR = "user.profile.avatar"
        const val KEY_LOCALE = "user.profile.locale"
        const val KEY_CURRENCY = "user.profile.currency"
        const val KEY_THEME = "user.profile.theme"
        const val KEY_PUSH = "user.profile.push"
        const val KEY_MEMBER_SINCE = "user.profile.member_since"
        const val KEY_PREMIUM = "user.profile.premium"
        const val KEY_TRIPS = "user.profile.trips"
        const val KEY_COUNTRIES = "user.profile.countries"
        const val KEY_WISHLIST = "user.profile.wishlist"
        const val KEY_PREF_LOCALE = "user.prefs.locale"
        const val KEY_PREF_CURRENCY = "user.prefs.currency"
        const val KEY_PREF_THEME = "user.prefs.theme"
        const val KEY_PREF_PUSH = "user.prefs.push"
        const val KEY_PREF_DARK = "user.prefs.dark"
    }
}
