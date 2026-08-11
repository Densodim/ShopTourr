package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.SyncDrainResult
import com.example.shoptourr.domain.repository.SyncRepository

class DrainSyncOutboxUseCase(
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(limit: Int = 20): Result<SyncDrainResult> =
        syncRepository.drainPending(limit)
}
