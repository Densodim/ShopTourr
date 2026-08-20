package com.example.shoptourr.domain.model

/**
 * Google Maps (Android) is only shown when a real API key is injected at build
 * time. Unreplaced Gradle placeholders must not count as configured.
 */
object NativeMapsConfig {
    fun isConfiguredApiKey(raw: String?): Boolean {
        val key = raw.orEmpty().trim()
        return key.isNotEmpty() && !key.contains("MAPS_API_KEY")
    }
}
