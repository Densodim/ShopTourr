package com.example.shoptourr.analytics

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsAnalyticsConsentStoreTest {

    @Test
    fun `consent defaults to denied and survives reload`() {
        val settings = MapSettings()
        val store = SettingsAnalyticsConsentStore(settings)
        assertEquals(false, store.isGranted())
        store.setGranted(true)
        assertEquals(true, SettingsAnalyticsConsentStore(settings).isGranted())
    }
}
