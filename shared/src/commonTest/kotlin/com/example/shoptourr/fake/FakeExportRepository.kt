package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeExportRepository(
    initial: ExportJob? = null,
    private val createError: Throwable? = null,
    private val refreshError: Throwable? = null,
    private val refreshSequence: List<ExportJob> = emptyList(),
) : ExportRepository {
    private val byTrip = MutableStateFlow(listOfNotNull(initial).associateBy { it.tripId })
    private var refreshIndex = 0
    var createCalls: Int = 0
        private set
    var refreshCalls: Int = 0
        private set

    fun setJob(job: ExportJob?) {
        byTrip.value = if (job == null) emptyMap() else mapOf(job.tripId to job)
    }

    override fun observeJob(tripId: String): Flow<ExportJob?> =
        byTrip.map { it[tripId] }

    override suspend fun create(tripId: String, draft: CreateExportDraft): Result<ExportJob> {
        createCalls += 1
        createError?.let { return Result.failure(it) }
        val job = ExportJob(
            id = "export-$createCalls",
            tripId = tripId,
            format = draft.format,
            status = ExportJobStatus.QUEUED,
            createdAt = "2026-01-01T00:00:00Z",
        )
        byTrip.value = byTrip.value + (tripId to job)
        return Result.success(job)
    }

    override suspend fun refreshJob(exportId: String): Result<ExportJob> {
        refreshCalls += 1
        refreshError?.let { return Result.failure(it) }
        val next = if (refreshSequence.isNotEmpty()) {
            refreshSequence[minOf(refreshIndex, refreshSequence.lastIndex)].also {
                refreshIndex = (refreshIndex + 1).coerceAtMost(refreshSequence.lastIndex)
            }
        } else {
            byTrip.value.values.firstOrNull { it.id == exportId }
                ?: return Result.failure(AppError.NotFound)
        }
        byTrip.value = byTrip.value + (next.tripId to next)
        return Result.success(next)
    }
}
