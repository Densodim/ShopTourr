package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportJob
import kotlinx.coroutines.flow.Flow

interface ExportRepository {
    fun observeJob(tripId: String): Flow<ExportJob?>
    suspend fun create(tripId: String, draft: CreateExportDraft): Result<ExportJob>
    suspend fun refreshJob(exportId: String): Result<ExportJob>
}
