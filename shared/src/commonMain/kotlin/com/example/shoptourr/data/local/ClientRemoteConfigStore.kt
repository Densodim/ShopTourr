package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.ClientRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ClientRemoteConfigStore {
    fun observe(): Flow<ClientRemoteConfig?>
    fun current(): ClientRemoteConfig?
    fun save(config: ClientRemoteConfig)
    fun clear()
}

class InMemoryClientRemoteConfigStore : ClientRemoteConfigStore {
    private val state = MutableStateFlow<ClientRemoteConfig?>(null)

    override fun observe(): Flow<ClientRemoteConfig?> = state.asStateFlow()
    override fun current(): ClientRemoteConfig? = state.value
    override fun save(config: ClientRemoteConfig) {
        state.value = config
    }
    override fun clear() {
        state.value = null
    }
}
