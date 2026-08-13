package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow

class ObserveWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
) {
    operator fun invoke(): Flow<List<WishlistItem>> = wishlistRepository.observeItems()
}

class RefreshWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
) {
    suspend operator fun invoke(): Result<Unit> = wishlistRepository.refresh()
}

class CreateWishlistItemUseCase(
    private val wishlistRepository: WishlistRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(draft: CreateWishlistDraft): Result<WishlistItem> {
        if (draft.name.trim().isEmpty()) return Result.failure(AppError.Validation("name"))
        if (draft.city.trim().isEmpty()) return Result.failure(AppError.Validation("city"))
        if (draft.targetPrice.minorUnits <= 0) return Result.failure(AppError.Validation("targetPrice"))
        return wishlistRepository.create(
            draft.copy(
                name = draft.name.trim(),
                city = draft.city.trim(),
                note = draft.note?.trim()?.ifEmpty { null },
            )
        ).onSuccess {
            drainSyncOutbox?.invoke()
        }
    }
}

class DeleteWishlistItemUseCase(
    private val wishlistRepository: WishlistRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) return Result.failure(AppError.Validation("id"))
        return wishlistRepository.delete(id).onSuccess {
            drainSyncOutbox?.invoke()
        }
    }
}
