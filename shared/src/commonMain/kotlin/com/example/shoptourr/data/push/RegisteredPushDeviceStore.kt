package com.example.shoptourr.data.push

import com.example.shoptourr.data.settings.SecureKeyValueStore

interface RegisteredPushDeviceStore {
    fun deviceId(): String?
    fun save(deviceId: String)
    fun clear()
}

class SecureRegisteredPushDeviceStore(
    private val secure: SecureKeyValueStore,
) : RegisteredPushDeviceStore {
    override fun deviceId(): String? = secure.getString(KEY)?.takeIf { it.isNotBlank() }

    override fun save(deviceId: String) {
        val trimmed = deviceId.trim()
        if (trimmed.isEmpty()) {
            clear()
        } else {
            secure.putString(KEY, trimmed)
        }
    }

    override fun clear() {
        secure.remove(KEY)
    }

    private companion object {
        const val KEY = "push.device_id"
    }
}

class InMemoryRegisteredPushDeviceStore : RegisteredPushDeviceStore {
    private var id: String? = null

    override fun deviceId(): String? = id

    override fun save(deviceId: String) {
        id = deviceId.trim().takeIf { it.isNotEmpty() }
    }

    override fun clear() {
        id = null
    }
}
