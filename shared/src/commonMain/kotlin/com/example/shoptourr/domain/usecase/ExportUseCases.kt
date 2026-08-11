package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow

class ObserveExportJobUseCase(
    private val exportRepository: ExportRepository,
) {
    operator fun invoke(tripId: String): Flow<ExportJob?> = exportRepository.observeJob(tripId)
}

class CreateExportUseCase(
    private val exportRepository: ExportRepository,
) {
    suspend operator fun invoke(tripId: String, draft: CreateExportDraft): Result<ExportJob> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return exportRepository.create(tripId, draft)
    }
}

class RefreshExportJobUseCase(
    private val exportRepository: ExportRepository,
) {
    suspend operator fun invoke(exportId: String): Result<ExportJob> {
        if (exportId.isBlank()) return Result.failure(AppError.Validation("exportId"))
        return exportRepository.refreshJob(exportId)
    }
}
