package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.SyncDrainResult
import com.example.shoptourr.domain.repository.SyncRepository

class FakeSyncRepository(
    var result: Result<SyncDrainResult> = Result.success(SyncDrainResult(0, 0)),
) : SyncRepository {
    var drainCalls: Int = 0
        private set
    var lastLimit: Int? = null
        private set

    override suspend fun drainPending(limit: Int): Result<SyncDrainResult> {
        drainCalls += 1
        lastLimit = limit
        return result
    }
}
