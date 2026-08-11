package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeWishlistRepository(
    initial: List<WishlistItem> = emptyList(),
    private val refreshError: Throwable? = null,
    private val mutateError: Throwable? = null,
) : WishlistRepository {
    private val state = MutableStateFlow(initial)

    val items: List<WishlistItem> get() = state.value

    var createCalls: Int = 0
        private set

    override fun observeItems(): Flow<List<WishlistItem>> = state.asStateFlow()

    override suspend fun refresh(): Result<Unit> {
        refreshError?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun create(draft: CreateWishlistDraft): Result<WishlistItem> {
        mutateError?.let { return Result.failure(it) }
        createCalls += 1
        val item = WishlistItem(
            id = "w-$createCalls",
            name = draft.name,
            city = draft.city,
            targetPrice = draft.targetPrice,
            iconEmoji = draft.iconEmoji,
            note = draft.note,
            createdAt = "2026-08-11T00:00:00Z",
        )
        state.update { it + item }
        return Result.success(item)
    }

    override suspend fun delete(id: String): Result<Unit> {
        mutateError?.let { return Result.failure(it) }
        if (state.value.none { it.id == id }) return Result.failure(AppError.NotFound)
        state.update { list -> list.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}
