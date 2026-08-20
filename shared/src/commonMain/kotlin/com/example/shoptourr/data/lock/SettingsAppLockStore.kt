package com.example.shoptourr.data.lock

import com.example.shoptourr.domain.lock.AppLockStore
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsAppLockStore(
    private val settings: Settings,
) : AppLockStore {
    override fun isEnabled(): Boolean = settings.getBoolean(KEY, false)

    override fun setEnabled(enabled: Boolean) {
        settings[KEY] = enabled
    }

    private companion object {
        const val KEY = "app.lock.enabled"
    }
}
