package com.example.shoptourr.analytics

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsAnalyticsConsentStore(
    private val settings: Settings,
) : AnalyticsConsentStore {
    override fun isGranted(): Boolean = settings.getBoolean(KEY, false)

    override fun setGranted(granted: Boolean) {
        settings[KEY] = granted
    }

    private companion object {
        const val KEY = "analytics.consent.granted"
    }
}
