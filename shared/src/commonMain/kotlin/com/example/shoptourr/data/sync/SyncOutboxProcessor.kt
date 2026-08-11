package com.example.shoptourr.data.sync

import com.example.shoptourr.api.common.MoneyDto
import com.example.shoptourr.api.purchase.CreatePurchaseRequest
import com.example.shoptourr.api.purchase.PurchaseCategory as ApiPurchaseCategory
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.VatCalculator
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CreatePurchasePayload(
    val localId: String,
    val tripId: String,
    val name: String,
    val category: String,
    val amount: String,
    val currency: String,
    val vatIncluded: Boolean,
    val vatRatePercent: String,
    val taxRefundEligible: Boolean,
    val place: String?,
    val purchaseDate: String?,
    val purchaseTime: String?,
)

data class DrainResult(
    val successCount: Int,
    val failureCount: Int,
)

class SyncOutboxProcessor(
    private val outbox: SyncOutbox,
    private val purchaseApi: PurchaseApi,
    private val purchaseLocalStore: PurchaseLocalStore,
    private val clock: () -> Long = { 0L },
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false },
) {
    suspend fun drainOnce(limit: Int = 20): DrainResult {
        var success = 0
        var failure = 0
        outbox.pending().take(limit).forEach { entry ->
            when (entry.type) {
                SyncMutationType.CREATE_PURCHASE -> {
                    runCatching {
                        val payload = json.decodeFromString<CreatePurchasePayload>(entry.payloadJson)
                        val response = purchaseApi.createPurchase(
                            tripId = payload.tripId,
                            request = CreatePurchaseRequest(
                                name = payload.name,
                                category = ApiPurchaseCategory.valueOf(payload.category),
                                amount = MoneyDto(payload.amount, payload.currency),
                                vatIncluded = payload.vatIncluded,
                                vatRatePercent = payload.vatRatePercent,
                                taxRefundEligible = payload.taxRefundEligible,
                                place = payload.place,
                                purchaseDate = payload.purchaseDate,
                                purchaseTime = payload.purchaseTime,
                            ),
                            idempotencyKey = entry.idempotencyKey,
                        )
                        val vat = VatCalculator.breakdown(
                            amount = Money.parse(response.amount.amount, response.amount.currency),
                            vatRatePercent = response.vat.vatRatePercent,
                            vatIncluded = response.vat.vatIncluded,
                        )
                        purchaseLocalStore.replaceId(
                            oldId = payload.localId,
                            purchase = Purchase(
                                id = response.id,
                                tripId = response.tripId,
                                name = response.name,
                                category = PurchaseCategory.valueOf(response.category.name),
                                amount = Money.parse(response.amount.amount, response.amount.currency),
                                vat = vat,
                                taxRefundEligible = response.taxRefundEligible,
                                place = response.place,
                                purchaseDate = response.purchaseDate,
                                purchaseTime = response.purchaseTime,
                                pendingSync = false,
                            ),
                        )
                        outbox.markSuccess(entry.id)
                        success += 1
                    }.onFailure {
                        outbox.markFailure(entry.id, updatedAtEpochMs = clock())
                        failure += 1
                    }
                }
                else -> {
                    // Not implemented yet — leave pending.
                }
            }
        }
        return DrainResult(successCount = success, failureCount = failure)
    }

    fun encodeCreatePurchasePayload(payload: CreatePurchasePayload): String =
        json.encodeToString(payload)
}
