package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.wishlist.CreateWishlistItemRequest
import com.example.shoptourr.data.remote.dto.wishlist.WishlistItemDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.data.sync.CreateWishlistPayload
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncPayloadCodec
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class WishlistRepositoryImpl(
    private val api: WishlistApi,
    private val localStore: WishlistLocalStore,
    private val outbox: SyncOutbox,
    private val idGenerator: () -> String,
    private val clock: () -> Long,
) : WishlistRepository {

    override fun observeItems(): Flow<List<WishlistItem>> = localStore.observe()

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val remote = api.fetchWishlist().items.map { it.toDomain() }
            localStore.replaceAll(remote)
        }.mapHttpAppError()

    override suspend fun create(draft: CreateWishlistDraft): Result<WishlistItem> =
        runCatching {
            val localId = idGenerator()
            val now = clock()
            val createdAt = Instant.fromEpochMilliseconds(now).toString()
            val item = WishlistItem(
                id = localId,
                name = draft.name,
                city = draft.city,
                targetPrice = draft.targetPrice,
                iconEmoji = draft.iconEmoji,
                note = draft.note,
                createdAt = createdAt,
            )
            localStore.upsert(item)
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-wishlist-$localId",
                    type = SyncMutationType.CREATE_WISHLIST,
                    payloadJson = SyncPayloadCodec.encodeWishlist(
                        CreateWishlistPayload(
                            localId = localId,
                            name = draft.name,
                            city = draft.city,
                            targetAmount = draft.targetPrice.toDecimalString(),
                            targetCurrency = draft.targetPrice.currency,
                            iconEmoji = draft.iconEmoji,
                            note = draft.note,
                        ),
                    ),
                    idempotencyKey = localId,
                    createdAtEpochMs = now,
                ),
            )
            item
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
