package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.repository.TripRepository

class CreateTripUseCase(
    private val tripRepository: TripRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(draft: CreateTripDraft): Result<TripSummary> {
        if (draft.city.trim().isEmpty()) return Result.failure(AppError.Validation("city"))
        if (draft.country.trim().isEmpty()) return Result.failure(AppError.Validation("country"))
        if (draft.budget.minorUnits <= 0) return Result.failure(AppError.Validation("budget"))
        if (draft.endDate < draft.startDate) return Result.failure(AppError.Validation("dates"))
        return tripRepository.createTrip(
            draft.copy(
                city = draft.city.trim(),
                country = draft.country.trim(),
            )
        ).also { result ->
            if (result.isSuccess) {
                drainSyncOutbox?.invoke()
            }
        }
    }
}
