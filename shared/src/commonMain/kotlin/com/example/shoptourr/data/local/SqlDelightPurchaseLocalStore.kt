package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shoptourr.db.PurchaseEntity
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.FtsQuery
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.VatBreakdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightPurchaseLocalStore(
    private val db: VoyageDatabase,
) : PurchaseLocalStore {

    override suspend fun upsert(purchase: Purchase) {
        withContext(Dispatchers.IO) {
            db.purchaseEntityQueries.upsert(
                id = purchase.id,
                trip_id = purchase.tripId,
                name = purchase.name,
                category = purchase.category.name,
                amount = purchase.amount.toDecimalString(),
                currency = purchase.amount.currency,
                vat_net = purchase.vat.net.toDecimalString(),
                vat_amount = purchase.vat.vat.toDecimalString(),
                vat_gross = purchase.vat.gross.toDecimalString(),
                vat_rate_percent = purchase.vat.vatRatePercent,
                vat_included = if (purchase.vat.vatIncluded) 1 else 0,
                tax_refund_eligible = if (purchase.taxRefundEligible) 1 else 0,
                place = purchase.place,
                purchase_date = purchase.purchaseDate,
                purchase_time = purchase.purchaseTime,
                pending_sync = if (purchase.pendingSync) 1 else 0,
                updated_at = purchase.updatedAt,
            )
            indexFts(purchase)
        }
    }

    override suspend fun replaceId(oldId: String, purchase: Purchase) = withContext(Dispatchers.IO) {
        db.transaction {
            db.purchaseFtsQueries.deleteByPurchaseId(oldId)
            db.purchaseEntityQueries.deleteById(oldId)
            db.purchaseEntityQueries.upsert(
                id = purchase.id,
                trip_id = purchase.tripId,
                name = purchase.name,
                category = purchase.category.name,
                amount = purchase.amount.toDecimalString(),
                currency = purchase.amount.currency,
                vat_net = purchase.vat.net.toDecimalString(),
                vat_amount = purchase.vat.vat.toDecimalString(),
                vat_gross = purchase.vat.gross.toDecimalString(),
                vat_rate_percent = purchase.vat.vatRatePercent,
                vat_included = if (purchase.vat.vatIncluded) 1 else 0,
                tax_refund_eligible = if (purchase.taxRefundEligible) 1 else 0,
                place = purchase.place,
                purchase_date = purchase.purchaseDate,
                purchase_time = purchase.purchaseTime,
                pending_sync = if (purchase.pendingSync) 1 else 0,
                updated_at = purchase.updatedAt,
            )
            indexFts(purchase)
        }
    }

    override suspend fun remove(id: String) {
        withContext(Dispatchers.IO) {
            db.purchaseFtsQueries.deleteByPurchaseId(id)
            db.purchaseEntityQueries.deleteById(id)
        }
    }

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        db.purchaseEntityQueries.selectByTrip(tripId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getById(id: String): Purchase? =
        db.purchaseEntityQueries.selectById(id).executeAsOneOrNull()?.toDomain()

    override fun searchByTrip(tripId: String, query: String): List<Purchase> {
        val match = FtsQuery.fromUserInput(query)
        val ftsHits = if (match == null) {
            emptyList()
        } else {
            db.purchaseFtsQueries.searchIdsByTrip(tripId, match).executeAsList()
                .mapNotNull { row ->
                    val id = row.purchase_id ?: return@mapNotNull null
                    db.purchaseEntityQueries.selectById(id).executeAsOneOrNull()?.toDomain()
                }
        }
        if (ftsHits.isNotEmpty() || match == null) {
            return if (match == null) {
                db.purchaseEntityQueries.selectByTrip(tripId).executeAsList().map { it.toDomain() }
            } else {
                ftsHits
            }
        }
        return db.purchaseEntityQueries.searchByTrip(tripId, query.trim()).executeAsList().map { it.toDomain() }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.purchaseFtsQueries.deleteAll()
            db.purchaseEntityQueries.deleteAll()
        }
    }

    private fun indexFts(purchase: Purchase) {
        db.purchaseFtsQueries.deleteByPurchaseId(purchase.id)
        db.purchaseFtsQueries.insert(
            purchase_id = purchase.id,
            trip_id = purchase.tripId,
            name = purchase.name,
            place = purchase.place.orEmpty(),
        )
    }

    private fun PurchaseEntity.toDomain(): Purchase {
        val currency = currency
        return Purchase(
            id = id,
            tripId = trip_id,
            name = name,
            category = PurchaseCategory.valueOf(category),
            amount = Money.parse(amount, currency),
            vat = VatBreakdown(
                net = Money.parse(vat_net, currency),
                vat = Money.parse(vat_amount, currency),
                gross = Money.parse(vat_gross, currency),
                vatRatePercent = vat_rate_percent,
                vatIncluded = vat_included == 1L,
            ),
            taxRefundEligible = tax_refund_eligible == 1L,
            place = place,
            purchaseDate = purchase_date,
            purchaseTime = purchase_time,
            pendingSync = pending_sync == 1L,
            updatedAt = updated_at,
        )
    }
}
