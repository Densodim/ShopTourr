package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.repository.AlertsRepository
import com.example.shoptourr.domain.repository.DiaryRepository
import com.example.shoptourr.domain.repository.TaxFreeRepository
import kotlinx.coroutines.flow.Flow

class ObserveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
) {
    operator fun invoke(tripId: String): Flow<List<DiaryDayGroup>> =
        diaryRepository.observeDiary(tripId)
}

class RefreshDiaryUseCase(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(tripId: String): Result<Unit> = diaryRepository.refresh(tripId)
}

class CreateDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(tripId: String, draft: CreateDiaryDraft): Result<DiaryEntry> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        if (draft.mood.trim().isEmpty()) return Result.failure(AppError.Validation("mood"))
        if (draft.text.trim().isEmpty()) return Result.failure(AppError.Validation("text"))
        return diaryRepository.create(
            tripId,
            draft.copy(mood = draft.mood.trim(), text = draft.text.trim()),
        ).onSuccess {
            drainSyncOutbox?.invoke()
        }
    }
}

class DeleteDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(tripId: String, entryId: String): Result<Unit> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        if (entryId.isBlank()) return Result.failure(AppError.Validation("entryId"))
        return diaryRepository.delete(tripId, entryId)
    }
}

class ObserveTaxFreeUseCase(
    private val taxFreeRepository: TaxFreeRepository,
) {
    operator fun invoke(tripId: String): Flow<TaxFreeSummary?> =
        taxFreeRepository.observeSummary(tripId)
}

class RefreshTaxFreeUseCase(
    private val taxFreeRepository: TaxFreeRepository,
) {
    suspend operator fun invoke(tripId: String): Result<TaxFreeSummary> =
        taxFreeRepository.refresh(tripId)
}

class ObserveAlertsUseCase(
    private val alertsRepository: AlertsRepository,
) {
    operator fun invoke(tripId: String): Flow<List<BudgetAlert>> =
        alertsRepository.observeAlerts(tripId)
}

class RefreshAlertsUseCase(
    private val alertsRepository: AlertsRepository,
) {
    suspend operator fun invoke(tripId: String): Result<Unit> =
        alertsRepository.refresh(tripId)
}
