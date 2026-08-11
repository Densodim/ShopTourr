package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.sync.CreatePurchasePayload
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class PurchaseRepositoryImpl(
    private val api: PurchaseApi,
    private val localStore: PurchaseLocalStore,
    private val outbox: SyncOutbox,
    private val idGenerator: () -> String,
    private val clock: () -> Long,
    private val json: Json = Json { encodeDefaults = true; explicitNulls = false },
) : PurchaseRepository {

    private val payloadCodec = SyncOutboxProcessor(
        outbox = outbox,
        purchaseApi = api,
        purchaseLocalStore = localStore,
        clock = clock,
        json = json,
    )

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
            )
            outbox.enqueue(
                SyncOutboxEntry(
                    id = "outbox-$localId",
                    type = SyncMutationType.CREATE_PURCHASE,
                    payloadJson = payloadCodec.encodeCreatePurchasePayload(payload),
                    idempotencyKey = localId,
                    createdAtEpochMs = now,
                )
            )
            purchase
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Result.failure(error as? AppError ?: AppError.Unknown(error.message))
            },
        )

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        localStore.observeByTrip(tripId)
}
