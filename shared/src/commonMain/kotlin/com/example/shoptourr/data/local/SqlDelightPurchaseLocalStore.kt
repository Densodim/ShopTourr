package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shoptourr.db.PurchaseEntity
import com.example.shoptourr.db.VoyageDatabase
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
            )
        }
    }

    override suspend fun replaceId(oldId: String, purchase: Purchase) = withContext(Dispatchers.IO) {
        db.transaction {
            db.purchaseEntityQueries.deleteById(oldId)
            // reuse upsert logic without nested withContext
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
            )
        }
    }

    override suspend fun remove(id: String) {
        withContext(Dispatchers.IO) {
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
        )
    }
}
