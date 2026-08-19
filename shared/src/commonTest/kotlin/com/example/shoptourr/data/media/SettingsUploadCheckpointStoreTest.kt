package com.example.shoptourr.data.media

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsUploadCheckpointStoreTest {

    @Test
    fun `save survives a new store instance on the same settings`() {
        val settings = MapSettings()
        SettingsUploadCheckpointStore(settings).save("media-1", 4096L)

        val reloaded = SettingsUploadCheckpointStore(settings)
        assertEquals(4096L, reloaded.offsetBytes("media-1"))
    }

    @Test
    fun `clearAll drops every checkpoint`() {
        val settings = MapSettings()
        val store = SettingsUploadCheckpointStore(settings)
        store.save("a", 1L)
        store.save("b", 2L)
        store.clearAll()
        assertEquals(0L, store.offsetBytes("a"))
        assertEquals(0L, SettingsUploadCheckpointStore(settings).offsetBytes("b"))
    }
}
