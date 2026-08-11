package com.example.shoptourr.data.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

interface TokenStore {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clear()
}

class SettingsTokenStore(
    private val settings: Settings,
) : TokenStore {
    override fun accessToken(): String? = settings.getStringOrNull(KEY_ACCESS)
    override fun refreshToken(): String? = settings.getStringOrNull(KEY_REFRESH)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        settings[KEY_ACCESS] = accessToken
        settings[KEY_REFRESH] = refreshToken
    }

    override fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
    }
}
