package com.example.shoptourr.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds a single pending navigation target from push / cold-start intents
 * until the Compose navigator can consume it.
 */
class PendingDeepLinkStore {
    private val pending = MutableStateFlow<VoyageNavigationTarget?>(null)

    fun observe(): Flow<VoyageNavigationTarget?> = pending.asStateFlow()

    fun offer(target: VoyageNavigationTarget) {
        pending.value = target
    }

    fun offerPushData(data: Map<String, String>) {
        VoyageDeepLinkRouter.resolvePushData(data)?.let(::offer)
    }

    fun consume(): VoyageNavigationTarget? {
        val value = pending.value
        pending.value = null
        return value
    }
}
