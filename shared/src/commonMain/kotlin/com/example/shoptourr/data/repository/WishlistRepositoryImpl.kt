package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.wishlist.CreateWishlistItemRequest
import com.example.shoptourr.data.remote.dto.wishlist.WishlistItemDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow

class WishlistRepositoryImpl(
    private val api: WishlistApi,
    private val localStore: WishlistLocalStore,
) : WishlistRepository {

    override fun observeItems(): Flow<List<WishlistItem>> = localStore.observe()

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val remote = api.fetchWishlist().items.map { it.toDomain() }
            localStore.replaceAll(remote)
        }.mapHttpAppError()

    override suspend fun create(draft: CreateWishlistDraft): Result<WishlistItem> =
        runCatching {
            val created = api.create(
                CreateWishlistItemRequest(
                    name = draft.name,
                    city = draft.city,
                    targetPrice = MoneyDto(draft.targetPrice.toDecimalString(), draft.targetPrice.currency),
                    iconEmoji = draft.iconEmoji,
                    note = draft.note,
                )
            ).toDomain()
            localStore.upsert(created)
            created
        }.mapHttpAppError()

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            api.delete(id)
            localStore.remove(id)
        }.mapHttpAppError()
}

private fun WishlistItemDto.toDomain(): WishlistItem =
    WishlistItem(
        id = id,
        name = name,
        city = city,
        targetPrice = Money.parse(targetPrice.amount, targetPrice.currency),
        iconEmoji = iconEmoji,
        note = note,
        createdAt = createdAt,
    )
