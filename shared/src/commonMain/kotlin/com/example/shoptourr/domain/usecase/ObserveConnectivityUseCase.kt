package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow

class ObserveConnectivityUseCase(
    private val connectivityMonitor: ConnectivityMonitor,
) {
    operator fun invoke(): Flow<Boolean> = connectivityMonitor.observeIsOnline()
}
