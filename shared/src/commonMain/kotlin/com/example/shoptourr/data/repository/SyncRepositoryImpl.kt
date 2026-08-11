package com.example.shoptourr.data.repository

import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.model.SyncDrainResult
import com.example.shoptourr.domain.repository.SyncRepository

class SyncRepositoryImpl(
    private val processor: SyncOutboxProcessor,
) : SyncRepository {
    override suspend fun drainPending(limit: Int): Result<SyncDrainResult> =
        runCatching {
            val result = processor.drainOnce(limit)
            SyncDrainResult(
                successCount = result.successCount,
                failureCount = result.failureCount,
            )
        }
}
