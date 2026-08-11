package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.repository.PurchaseRepository

class CreatePurchaseUseCase(
    private val purchaseRepository: PurchaseRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(tripId: String, draft: PurchaseDraft): Result<Purchase> {
        if (draft.name.trim().isEmpty()) {
            return Result.failure(AppError.Validation("name"))
        }
        if (draft.amount.minorUnits <= 0) {
            return Result.failure(AppError.Validation("amount"))
        }
        if (tripId.isBlank()) {
            return Result.failure(AppError.Validation("tripId"))
        }
        return purchaseRepository.create(tripId, draft.copy(name = draft.name.trim()))
            .also { result ->
                if (result.isSuccess) {
                    drainSyncOutbox?.invoke()
                }
            }
    }
}
