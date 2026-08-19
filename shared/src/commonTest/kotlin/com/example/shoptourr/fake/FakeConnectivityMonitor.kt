package com.example.shoptourr.fake

import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeConnectivityMonitor(
    initiallyOnline: Boolean = true,
) : ConnectivityMonitor {
    private val online = MutableStateFlow(initiallyOnline)

    fun setOnline(value: Boolean) {
        online.value = value
    }

    override fun observeIsOnline(): Flow<Boolean> = online.asStateFlow()

    override fun currentIsOnline(): Boolean = online.value
}
