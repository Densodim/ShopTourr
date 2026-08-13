package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.dto.purchase.toDomainPurchase
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.data.sync.CreatePurchasePayload
import com.example.shoptourr.data.sync.DeletePurchasePayload
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncPayloadCodec
import com.example.shoptourr.data.sync.UpdatePurchasePayload
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.PurchasePageRequest
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow

class PurchaseRepositoryImpl(
    private val api: PurchaseApi,
    private val localStore: PurchaseLocalStore,
    private val outbox: SyncOutbox,
    private val idGenerator: () -> String,
    private val clock: () -> Long,
) : PurchaseRepository {

    override suspend fun create(tripId: String, draft: PurchaseDraft): Result<Purchase> =
        runCatching {
            val localId = idGenerator()
            val now = clock()
            val vat = VatCalculator.breakdown(
                amount = draft.amount,
                vatRatePercent = draft.vatRatePercent,
                vatIncluded = draft.vatIncluded,
            )
            val purchase = Purchase(
                id = localId,
                tripId = tripId,
                name = draft.name,
                category = draft.category,
                amount = vat.gross,
                vat = vat,
                taxRefundEligible = draft.taxRefundEligible,
                place = draft.place,
                purchaseDate = draft.purchaseDate ?: "1970-01-01",
                purchaseTime = draft.purchaseTime,
                pendingSync = true,
            )
            localStore.upsert(purchase)
            val payload = CreatePurchasePayload(
                localId = localId,
                tripId = tripId,
                name = draft.name,
                category = draft.category.name,
                amount = draft.amount.toDecimalString(),
                currency = draft.amount.currency,
                vatIncluded = draft.vatIncluded,
                vatRatePercent = draft.vatRatePercent,
                taxRefundEligible = draft.taxRefundEligible,
                place = draft.place,
                purchaseDate = draft.purchaseDate,
                purchaseTime = draft.purchaseTime,
                receiptMediaId = draft.receiptMediaId,
                splitWithTravelerIds = draft.splitWithTravelerIds,
            )
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-$localId",
                    type = SyncMutationType.CREATE_PURCHASE,
                    payloadJson = SyncPayloadCodec.encodePurchase(payload),
                    idempotencyKey = localId,
                    createdAtEpochMs = now,
                )
            )
            purchase
        }.mapHttpAppError()

    override suspend fun update(
        tripId: String,
        purchaseId: String,
        draft: PurchaseDraft,
    ): Result<Purchase> =
        runCatching {
            val now = clock()
            val existing = localStore.getById(purchaseId)
                ?: error("purchase not found: $purchaseId")
            val vat = VatCalculator.breakdown(
                amount = draft.amount,
                vatRatePercent = draft.vatRatePercent,
                vatIncluded = draft.vatIncluded,
            )
            val purchase = existing.copy(
                name = draft.name,
                category = draft.category,
                amount = vat.gross,
                vat = vat,
                taxRefundEligible = draft.taxRefundEligible,
                place = draft.place,
                purchaseDate = draft.purchaseDate ?: existing.purchaseDate,
                purchaseTime = draft.purchaseTime,
                pendingSync = true,
            )
            localStore.upsert(purchase)
            val payload = UpdatePurchasePayload(
                purchaseId = purchaseId,
                tripId = tripId,
                name = draft.name,
                category = draft.category.name,
                amount = draft.amount.toDecimalString(),
                currency = draft.amount.currency,
                vatIncluded = draft.vatIncluded,
                vatRatePercent = draft.vatRatePercent,
                taxRefundEligible = draft.taxRefundEligible,
                place = draft.place,
                purchaseDate = draft.purchaseDate,
                purchaseTime = draft.purchaseTime,
                receiptMediaId = draft.receiptMediaId,
                splitWithTravelerIds = draft.splitWithTravelerIds,
            )
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-upd-$purchaseId-$now",
                    type = SyncMutationType.UPDATE_PURCHASE,
                    payloadJson = SyncPayloadCodec.encodeUpdatePurchase(payload),
                    idempotencyKey = "upd-$purchaseId-$now",
                    createdAtEpochMs = now,
                ),
            )
            purchase
        }.mapHttpAppError()

    override suspend fun delete(tripId: String, purchaseId: String): Result<Unit> =
        runCatching {
            val now = clock()
            localStore.remove(purchaseId)
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-del-$purchaseId-$now",
                    type = SyncMutationType.DELETE_PURCHASE,
                    payloadJson = SyncPayloadCodec.encodeDeletePurchase(
                        DeletePurchasePayload(purchaseId = purchaseId, tripId = tripId),
                    ),
                    idempotencyKey = "del-$purchaseId-$now",
                    createdAtEpochMs = now,
                ),
            )
        }.mapHttpAppError()

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        localStore.observeByTrip(tripId)

    override suspend fun refreshPage(
        tripId: String,
        request: PurchasePageRequest,
    ): Result<List<Purchase>> =
        runCatching {
            val response = api.fetchPurchases(
                tripId = tripId,
                page = request.page,
                size = request.size,
                afterDate = request.afterDate,
                afterId = request.afterId,
            )
            val purchases = response.days.flatMap { day -> day.items }.map { dto ->
                dto.toDomainPurchase(pendingSync = false)
            }
            purchases.forEach { localStore.upsert(it) }
            purchases
        }.mapHttpAppError()
}
