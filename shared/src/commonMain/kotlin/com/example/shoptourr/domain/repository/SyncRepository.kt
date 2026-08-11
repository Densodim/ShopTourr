package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.SyncDrainResult

interface SyncRepository {
    suspend fun drainPending(limit: Int = 20): Result<SyncDrainResult>
}
