package com.example.shoptourr.data.sync

import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.analytics.NoOpAnalytics
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Drains the mutation outbox and analytics queue whenever connectivity flips to online.
 */
class SyncScheduler(
    private val connectivity: ConnectivityMonitor,
    private val drainSyncOutbox: DrainSyncOutboxUseCase,
    private val analytics: Analytics = NoOpAnalytics,
) {
    private var started = false
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        job = scope.launch {
            connectivity.observeIsOnline()
                .distinctUntilChanged()
                .filter { online -> online }
                .collect {
                    drainSyncOutbox()
                    analytics.flush()
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        started = false
    }
}
