package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.db.WishlistEntity
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightWishlistLocalStore(
    private val db: VoyageDatabase,
) : WishlistLocalStore {

    override fun observe(): Flow<List<WishlistItem>> =
        db.wishlistEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun all(): List<WishlistItem> =
        db.wishlistEntityQueries.selectAll().executeAsList().map { it.toDomain() }

    override suspend fun replaceAll(items: List<WishlistItem>) = withContext(Dispatchers.IO) {
        db.transaction {
            db.wishlistEntityQueries.deleteAll()
            items.forEach { upsertInternal(it) }
        }
    }

    override suspend fun upsert(item: WishlistItem) = withContext(Dispatchers.IO) {
        upsertInternal(item)
    }

    override suspend fun remove(id: String) {
        withContext(Dispatchers.IO) {
            db.wishlistEntityQueries.deleteById(id)
        }
    }

    private fun upsertInternal(item: WishlistItem) {
        db.wishlistEntityQueries.upsert(
            id = item.id,
            name = item.name,
            city = item.city,
            price_amount = item.targetPrice.toDecimalString(),
            price_currency = item.targetPrice.currency,
            icon_emoji = item.iconEmoji,
            note = item.note,
            created_at = item.createdAt,
        )
    }

    private fun WishlistEntity.toDomain(): WishlistItem =
        WishlistItem(
            id = id,
            name = name,
            city = city,
            targetPrice = Money.parse(price_amount, price_currency),
            iconEmoji = icon_emoji,
            note = note,
            createdAt = created_at,
        )
}
