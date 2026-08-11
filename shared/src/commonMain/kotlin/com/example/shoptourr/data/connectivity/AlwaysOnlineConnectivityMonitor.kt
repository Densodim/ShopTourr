package com.example.shoptourr.data.connectivity

import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Default / iOS / tests until NWPathMonitor is wired. */
class AlwaysOnlineConnectivityMonitor : ConnectivityMonitor {
    override fun observeIsOnline(): Flow<Boolean> = MutableStateFlow(true).asStateFlow()
}
