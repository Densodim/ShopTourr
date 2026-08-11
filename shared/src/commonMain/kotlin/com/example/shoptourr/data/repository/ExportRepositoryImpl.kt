package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.ExportLocalStore
import com.example.shoptourr.data.remote.ExportApi
import com.example.shoptourr.data.remote.dto.export.CreateExportRequest
import com.example.shoptourr.data.remote.dto.export.ExportFormat as ApiExportFormat
import com.example.shoptourr.data.remote.dto.export.ExportJobDto
import com.example.shoptourr.data.remote.dto.export.ExportJobStatus as ApiExportJobStatus
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow

class ExportRepositoryImpl(
    private val api: ExportApi,
    private val localStore: ExportLocalStore,
) : ExportRepository {
    override fun observeJob(tripId: String): Flow<ExportJob?> = localStore.observe(tripId)

    override suspend fun create(tripId: String, draft: CreateExportDraft): Result<ExportJob> =
        runCatching {
            val job = api.create(
                tripId = tripId,
                request = CreateExportRequest(
                    format = draft.format.toApi(),
                    includeTaxFree = draft.includeTaxFree,
                    includeDiary = draft.includeDiary,
                ),
            ).toDomain()
            localStore.save(job)
            job
        }.mapHttpAppError()

    override suspend fun refreshJob(exportId: String): Result<ExportJob> =
        runCatching {
            val job = api.fetchJob(exportId).toDomain()
            localStore.save(job)
            job
        }.mapHttpAppError()
}

private fun ExportJobDto.toDomain(): ExportJob =
    ExportJob(
        id = id,
        tripId = tripId,
        format = format.toDomain(),
        status = status.toDomain(),
        downloadUrl = downloadUrl,
        expiresAt = expiresAt,
        errorCode = errorCode,
        createdAt = createdAt,
        finishedAt = finishedAt,
    )

private fun ExportFormat.toApi(): ApiExportFormat = when (this) {
    ExportFormat.PDF -> ApiExportFormat.PDF
    ExportFormat.CSV -> ApiExportFormat.CSV
}

private fun ApiExportFormat.toDomain(): ExportFormat = when (this) {
    ApiExportFormat.PDF -> ExportFormat.PDF
    ApiExportFormat.CSV -> ExportFormat.CSV
}

private fun ApiExportJobStatus.toDomain(): ExportJobStatus = when (this) {
    ApiExportJobStatus.QUEUED -> ExportJobStatus.QUEUED
    ApiExportJobStatus.RUNNING -> ExportJobStatus.RUNNING
    ApiExportJobStatus.READY -> ExportJobStatus.READY
    ApiExportJobStatus.FAILED -> ExportJobStatus.FAILED
    ApiExportJobStatus.EXPIRED -> ExportJobStatus.EXPIRED
}
