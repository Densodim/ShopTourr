package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.repository.SyncConflictNotifier
import kotlinx.coroutines.flow.Flow

class ObserveSyncConflictsUseCase(
    private val notifier: SyncConflictNotifier,
) {
    operator fun invoke(): Flow<Boolean> = notifier.observe()
}

class AcknowledgeSyncConflictUseCase(
    private val notifier: SyncConflictNotifier,
) {
    operator fun invoke() = notifier.acknowledge()
}
