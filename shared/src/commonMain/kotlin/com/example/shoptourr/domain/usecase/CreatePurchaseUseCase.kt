package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.repository.PurchaseRepository
import com.example.shoptourr.domain.validation.FieldRules
import com.example.shoptourr.domain.validation.PLACE_FIELD_MAX

class CreatePurchaseUseCase(
    private val purchaseRepository: PurchaseRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(tripId: String, draft: PurchaseDraft): Result<Purchase> {
        val name = draft.name.trim()
        if (!FieldRules.isItemName(name)) {
            return Result.failure(AppError.Validation("name"))
        }
        if (draft.amount.minorUnits <= 0 || !FieldRules.isIso4217(draft.amount.currency)) {
            return Result.failure(AppError.Validation("amount"))
        }
        if (tripId.isBlank()) {
            return Result.failure(AppError.Validation("tripId"))
        }
        val place = draft.place?.trim()?.takeIf { it.isNotEmpty() }
        if (place != null && !FieldRules.isItemName(place, max = PLACE_FIELD_MAX)) {
            return Result.failure(AppError.Validation("place"))
        }
        val purchaseDate = draft.purchaseDate
        if (purchaseDate != null && !FieldRules.isIsoDate(purchaseDate)) {
            return Result.failure(AppError.Validation("purchaseDate"))
        }
        val purchaseTime = draft.purchaseTime
        if (purchaseTime != null && !FieldRules.isIsoTime(purchaseTime)) {
            return Result.failure(AppError.Validation("purchaseTime"))
        }
        return purchaseRepository.create(
            tripId,
            draft.copy(name = name, place = place),
        ).also { result ->
            if (result.isSuccess) {
                drainSyncOutbox?.invoke()
            }
        }
    }
}

class RefreshPurchasesUseCase(
    private val purchaseRepository: PurchaseRepository,
) {
    suspend operator fun invoke(
        tripId: String,
        request: com.example.shoptourr.domain.model.PurchasePageRequest =
            com.example.shoptourr.domain.model.PurchasePageRequest(),
    ): Result<List<Purchase>> {
        if (tripId.isBlank()) return Result.failure(AppError.Validation("tripId"))
        return purchaseRepository.refreshPage(tripId, request)
    }
}
