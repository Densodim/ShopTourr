package com.example.shoptourr.data

import com.example.shoptourr.data.local.SettingsClientRemoteConfigStore
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlags
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsClientRemoteConfigStoreTest {

    private val config = ClientRemoteConfig(
        minAndroidBuild = 20,
        minIosBuild = 15,
        softMinAndroidBuild = 25,
        flags = FeatureFlags(exportPdf = true, ocrAssist = false, nativeMaps = true),
        storeUrlAndroid = "https://play.google.com/voyage",
    )

    @Test
    fun `save survives a new store instance on the same settings`() {
        val settings = MapSettings()
        SettingsClientRemoteConfigStore(settings).save(config)

        val reloaded = SettingsClientRemoteConfigStore(settings)
        assertEquals(20, reloaded.current()?.minAndroidBuild)
        assertEquals(false, reloaded.current()?.flags?.ocrAssist)
        assertTrue(reloaded.current()?.flags?.nativeMaps == true)
        assertEquals("https://play.google.com/voyage", reloaded.current()?.storeUrlAndroid)
    }

    @Test
    fun `clear removes persisted config`() {
        val settings = MapSettings()
        val store = SettingsClientRemoteConfigStore(settings)
        store.save(config)
        store.clear()
        assertNull(store.current())
        assertNull(SettingsClientRemoteConfigStore(settings).current())
    }
}
