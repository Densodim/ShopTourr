package com.example.shoptourr.data.sync

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.purchase.CreatePurchaseRequest
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory as ApiPurchaseCategory
import com.example.shoptourr.data.remote.dto.trip.CreateTripRequest
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
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
    val receiptMediaId: String? = null,
)

@Serializable
data class CreateTripPayload(
    val localId: String,
    val city: String,
    val country: String,
    val countryCode: String?,
    val startDate: String,
    val endDate: String,
    val budgetAmount: String,
    val budgetCurrency: String,
    val defaultVatRatePercent: String?,
)

data class DrainResult(
    val successCount: Int,
    val failureCount: Int,
)

object SyncPayloadCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    fun encodePurchase(payload: CreatePurchasePayload): String = json.encodeToString(payload)
    fun encodeTrip(payload: CreateTripPayload): String = json.encodeToString(payload)
    fun decodePurchase(raw: String): CreatePurchasePayload = json.decodeFromString(raw)
    fun decodeTrip(raw: String): CreateTripPayload = json.decodeFromString(raw)
}

class SyncOutboxProcessor(
    private val outbox: SyncOutbox,
    private val purchaseApi: PurchaseApi,
    private val purchaseLocalStore: PurchaseLocalStore,
    private val tripApi: TripApi,
    private val tripLocalStore: TripLocalStore,
    private val clock: () -> Long = { 0L },
) {
    suspend fun drainOnce(limit: Int = 20): DrainResult {
        var success = 0
        var failure = 0
        outbox.pending().take(limit).forEach { entry ->
            val ok = runCatching {
                when (entry.type) {
                    SyncMutationType.CREATE_PURCHASE -> drainCreatePurchase(entry)
                    SyncMutationType.CREATE_TRIP -> drainCreateTrip(entry)
                    else -> return@forEach
                }
            }.isSuccess
            if (ok) {
                outbox.markSuccess(entry.id)
                success += 1
            } else {
                outbox.markFailure(entry.id, updatedAtEpochMs = clock())
                failure += 1
            }
        }
        return DrainResult(successCount = success, failureCount = failure)
    }

    private suspend fun drainCreatePurchase(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodePurchase(entry.payloadJson)
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
                receiptMediaId = payload.receiptMediaId,
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
    }

    private suspend fun drainCreateTrip(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeTrip(entry.payloadJson)
        val response = tripApi.createTrip(
            request = CreateTripRequest(
                city = payload.city,
                country = payload.country,
                countryCode = payload.countryCode,
                startDate = payload.startDate,
                endDate = payload.endDate,
                budget = MoneyDto(payload.budgetAmount, payload.budgetCurrency),
                defaultVatRatePercent = payload.defaultVatRatePercent,
            ),
            idempotencyKey = entry.idempotencyKey,
        )
        val serverTrip = TripSummary(
            id = response.id,
            city = response.city,
            country = response.country,
            status = TripStatus.valueOf(response.status.name),
            startDate = response.startDate,
            endDate = response.endDate,
            budget = Money.parse(response.budget.amount, response.budget.currency),
            spent = Money.parse(response.spent.amount, response.spent.currency),
            purchaseCount = response.purchaseCount,
            flagEmoji = response.flagEmoji,
            datesLabel = response.datesLabel,
            currentDayNumber = response.currentDayNumber,
            dayCount = response.dayCount,
        )
        val replaced = tripLocalStore.all().map { if (it.id == payload.localId) serverTrip else it }
        tripLocalStore.replaceAll(replaced)
    }
}
