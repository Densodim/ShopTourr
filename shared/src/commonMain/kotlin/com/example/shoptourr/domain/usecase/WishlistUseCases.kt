package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.WishlistRepository
import com.example.shoptourr.domain.validation.FieldRules
import com.example.shoptourr.domain.validation.MOOD_MAX
import com.example.shoptourr.domain.validation.NOTE_MAX
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
        val name = draft.name.trim()
        val city = draft.city.trim()
        if (!FieldRules.isItemName(name)) return Result.failure(AppError.Validation("name"))
        if (!FieldRules.isPlaceName(city)) return Result.failure(AppError.Validation("city"))
        if (draft.targetPrice.minorUnits <= 0 || !FieldRules.isIso4217(draft.targetPrice.currency)) {
            return Result.failure(AppError.Validation("targetPrice"))
        }
        val icon = draft.iconEmoji?.trim()?.takeIf { it.isNotEmpty() }
        if (icon != null && (icon.length > MOOD_MAX || !FieldRules.isMood(icon))) {
            return Result.failure(AppError.Validation("iconEmoji"))
        }
        val note = draft.note?.trim()?.takeIf { it.isNotEmpty() }
        if (note != null && !FieldRules.isFreeText(note, max = NOTE_MAX)) {
            return Result.failure(AppError.Validation("note"))
        }
        return wishlistRepository.create(
            draft.copy(
                name = name,
                city = city,
                iconEmoji = icon,
                note = note,
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
