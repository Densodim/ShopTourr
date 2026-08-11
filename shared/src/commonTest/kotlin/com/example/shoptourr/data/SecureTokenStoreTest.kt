package com.example.shoptourr.data

import com.example.shoptourr.data.settings.InMemorySecureKeyValueStore
import com.example.shoptourr.data.settings.SecureTokenStore
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecureTokenStoreTest {

    @Test
    fun `save load and clear tokens in secure store`() {
        val store = SecureTokenStore(InMemorySecureKeyValueStore())
        assertNull(store.accessToken())

        store.saveTokens(accessToken = "a", refreshToken = "r")
        assertEquals("a", store.accessToken())
        assertEquals("r", store.refreshToken())

        store.clear()
        assertNull(store.accessToken())
        assertNull(store.refreshToken())
    }

    @Test
    fun `migrates tokens from legacy settings once`() {
        val legacy = SettingsTokenStore(MapSettings())
        legacy.saveTokens("legacy-a", "legacy-r")
        val secure = InMemorySecureKeyValueStore()

        val store = SecureTokenStore(secure = secure, legacy = legacy)
        assertEquals("legacy-a", store.accessToken())
        assertEquals("legacy-r", store.refreshToken())
        assertNull(legacy.accessToken())
        assertNull(legacy.refreshToken())
    }

    @Test
    fun `does not overwrite secure tokens with legacy`() {
        val legacy = SettingsTokenStore(MapSettings())
        legacy.saveTokens("legacy-a", "legacy-r")
        val secure = InMemorySecureKeyValueStore().apply {
            putString("auth.access_token", "secure-a")
            putString("auth.refresh_token", "secure-r")
        }

        val store = SecureTokenStore(secure = secure, legacy = legacy)
        assertEquals("secure-a", store.accessToken())
        assertEquals("secure-r", store.refreshToken())
    }
}
