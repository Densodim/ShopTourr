package com.example.shoptourr.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Server-wins conflict notices from outbox drain (409 reconciled).
 */
interface SyncConflictNotifier {
    fun observe(): Flow<Boolean>
    fun reportServerWins()
    fun acknowledge()
}
