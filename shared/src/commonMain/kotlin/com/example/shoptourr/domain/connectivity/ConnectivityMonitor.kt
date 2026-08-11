package com.example.shoptourr.domain.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityMonitor {
    fun observeIsOnline(): Flow<Boolean>
}
