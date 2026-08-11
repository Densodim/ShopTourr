package com.example.shoptourr.data.settings

/**
 * Platform-backed secure key/value storage for secrets (JWT).
 * Prefer Keychain / EncryptedSharedPreferences over plain Settings.
 */
interface SecureKeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

class InMemorySecureKeyValueStore : SecureKeyValueStore {
    private val map = mutableMapOf<String, String>()

    override fun getString(key: String): String? = map[key]

    override fun putString(key: String, value: String) {
        map[key] = value
    }

    override fun remove(key: String) {
        map.remove(key)
    }
}

class SecureTokenStore(
    private val secure: SecureKeyValueStore,
    private val legacy: TokenStore? = null,
) : TokenStore {
    init {
        migrateFromLegacyIfNeeded()
    }

    override fun accessToken(): String? = secure.getString(KEY_ACCESS)

    override fun refreshToken(): String? = secure.getString(KEY_REFRESH)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        secure.putString(KEY_ACCESS, accessToken)
        secure.putString(KEY_REFRESH, refreshToken)
        legacy?.clear()
    }

    override fun clear() {
        secure.remove(KEY_ACCESS)
        secure.remove(KEY_REFRESH)
        legacy?.clear()
    }

    private fun migrateFromLegacyIfNeeded() {
        val legacyStore = legacy ?: return
        if (secure.getString(KEY_ACCESS) != null) return
        val access = legacyStore.accessToken() ?: return
        val refresh = legacyStore.refreshToken() ?: return
        secure.putString(KEY_ACCESS, access)
        secure.putString(KEY_REFRESH, refresh)
        legacyStore.clear()
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
    }
}
