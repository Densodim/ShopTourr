package com.example.shoptourr.data.sync

import com.example.shoptourr.domain.repository.SyncConflictNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySyncConflictNotifier : SyncConflictNotifier {
    private val visible = MutableStateFlow(false)

    override fun observe(): Flow<Boolean> = visible.asStateFlow()

    override fun reportServerWins() {
        visible.value = true
    }

    override fun acknowledge() {
        visible.value = false
    }
}
