package com.example.shoptourr.data

import com.example.shoptourr.data.lock.SettingsAppLockStore
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsAppLockStoreTest {

    @Test
    fun `lock defaults to off and survives reload`() {
        val settings = MapSettings()
        val store = SettingsAppLockStore(settings)
        assertEquals(false, store.isEnabled())
        store.setEnabled(true)
        assertEquals(true, SettingsAppLockStore(settings).isEnabled())
    }
}
