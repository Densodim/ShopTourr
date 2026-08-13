package com.example.shoptourr.data.sync

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.diary.CreateDiaryEntryRequest
import com.example.shoptourr.data.remote.dto.purchase.CreatePurchaseRequest
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory as ApiPurchaseCategory
import com.example.shoptourr.data.remote.dto.trip.CreateTravelerRequest
import com.example.shoptourr.data.remote.dto.trip.CreateTripRequest
import com.example.shoptourr.data.remote.dto.wishlist.CreateWishlistItemRequest
import com.example.shoptourr.data.local.DiaryLocalStore
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.data.remote.DiaryApi
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.repository.SyncConflictNotifier
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
    val splitWithTravelerIds: List<String> = emptyList(),
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
    val quoteCurrency: String? = null,
    val travelers: List<Traveler> = emptyList(),
) {
    @Serializable
    data class Traveler(
        val name: String,
        val colorHex: String,
        val avatarGlyph: String? = null,
    )
}

@Serializable
data class CreateWishlistPayload(
    val localId: String,
    val name: String,
    val city: String,
    val targetAmount: String,
    val targetCurrency: String,
    val iconEmoji: String? = null,
    val note: String? = null,
)

@Serializable
data class CreateDiaryPayload(
    val localId: String,
    val tripId: String,
    val entryDate: String? = null,
    val mood: String,
    val text: String,
)

@Serializable
data class UpdatePurchasePayload(
    val purchaseId: String,
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
    val splitWithTravelerIds: List<String> = emptyList(),
)

@Serializable
data class DeletePurchasePayload(
    val purchaseId: String,
    val tripId: String,
)

@Serializable
data class UpdateTripPayload(
    val tripId: String,
    val city: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budgetAmount: String? = null,
    val budgetCurrency: String? = null,
    val defaultVatRatePercent: String? = null,
    val status: String? = null,
)

@Serializable
data class DeleteTripPayload(
    val tripId: String,
)

@Serializable
data class DeleteWishlistPayload(
    val itemId: String,
)

@Serializable
data class DeleteDiaryPayload(
    val tripId: String,
    val entryId: String,
)

data class DrainResult(
    val successCount: Int,
    val failureCount: Int,
    val skippedCount: Int = 0,
)

object SyncPayloadCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    fun encodePurchase(payload: CreatePurchasePayload): String = json.encodeToString(payload)
    fun encodeTrip(payload: CreateTripPayload): String = json.encodeToString(payload)
    fun encodeWishlist(payload: CreateWishlistPayload): String = json.encodeToString(payload)
    fun encodeDiary(payload: CreateDiaryPayload): String = json.encodeToString(payload)
    fun encodeUpdatePurchase(payload: UpdatePurchasePayload): String = json.encodeToString(payload)
    fun encodeDeletePurchase(payload: DeletePurchasePayload): String = json.encodeToString(payload)
    fun encodeUpdateTrip(payload: UpdateTripPayload): String = json.encodeToString(payload)
    fun encodeDeleteTrip(payload: DeleteTripPayload): String = json.encodeToString(payload)
    fun encodeDeleteWishlist(payload: DeleteWishlistPayload): String = json.encodeToString(payload)
    fun encodeDeleteDiary(payload: DeleteDiaryPayload): String = json.encodeToString(payload)

    fun decodePurchase(raw: String): CreatePurchasePayload = json.decodeFromString(raw)
    fun decodeTrip(raw: String): CreateTripPayload = json.decodeFromString(raw)
    fun decodeWishlist(raw: String): CreateWishlistPayload = json.decodeFromString(raw)
    fun decodeDiary(raw: String): CreateDiaryPayload = json.decodeFromString(raw)
    fun decodeUpdatePurchase(raw: String): UpdatePurchasePayload = json.decodeFromString(raw)
    fun decodeDeletePurchase(raw: String): DeletePurchasePayload = json.decodeFromString(raw)
    fun decodeUpdateTrip(raw: String): UpdateTripPayload = json.decodeFromString(raw)
    fun decodeDeleteTrip(raw: String): DeleteTripPayload = json.decodeFromString(raw)
    fun decodeDeleteWishlist(raw: String): DeleteWishlistPayload = json.decodeFromString(raw)
    fun decodeDeleteDiary(raw: String): DeleteDiaryPayload = json.decodeFromString(raw)
}

class SyncOutboxProcessor(
    private val outbox: SyncOutbox,
    private val purchaseApi: PurchaseApi,
    private val purchaseLocalStore: PurchaseLocalStore,
    private val tripApi: TripApi,
    private val tripLocalStore: TripLocalStore,
    private val wishlistApi: WishlistApi,
    private val wishlistLocalStore: WishlistLocalStore,
    private val diaryApi: DiaryApi,
    private val diaryLocalStore: DiaryLocalStore,
    private val clock: () -> Long = { 0L },
    private val conflictNotifier: SyncConflictNotifier? = null,
) {
    suspend fun drainOnce(limit: Int = 20): DrainResult {
        var success = 0
        var failure = 0
        var skipped = 0
        val now = clock()
        outbox.pending()
            .asSequence()
            .filter { SyncOutboxPolicy.isDue(it, now) }
            .take(limit)
            .forEach { entry ->
                val outcome = runCatching {
                    when (entry.type) {
                        SyncMutationType.CREATE_PURCHASE -> drainCreatePurchase(entry)
                        SyncMutationType.UPDATE_PURCHASE -> drainUpdatePurchase(entry)
                        SyncMutationType.DELETE_PURCHASE -> drainDeletePurchase(entry)
                        SyncMutationType.CREATE_TRIP -> drainCreateTrip(entry)
                        SyncMutationType.UPDATE_TRIP -> drainUpdateTrip(entry)
                        SyncMutationType.DELETE_TRIP -> drainDeleteTrip(entry)
                        SyncMutationType.CREATE_WISHLIST -> drainCreateWishlist(entry)
                        SyncMutationType.DELETE_WISHLIST -> drainDeleteWishlist(entry)
                        SyncMutationType.CREATE_DIARY -> drainCreateDiary(entry)
                        SyncMutationType.DELETE_DIARY -> drainDeleteDiary(entry)
                    }
                }
                when {
                    outcome.isSuccess -> {
                        outbox.markSuccess(entry.id)
                        success += 1
                    }
                    outcome.exceptionOrNull() is AppError.Conflict -> {
                        val reconciled = runCatching { reconcileServerWins(entry) }
                        if (reconciled.isSuccess) {
                            outbox.markSuccess(entry.id)
                            success += 1
                            conflictNotifier?.reportServerWins()
                        } else {
                            outbox.markFailure(entry.id, updatedAtEpochMs = clock())
                            failure += 1
                        }
                    }
                    else -> {
                        outbox.markFailure(entry.id, updatedAtEpochMs = clock())
                        failure += 1
                    }
                }
            }
        skipped = outbox.pending().count { !SyncOutboxPolicy.isDue(it, now) }
        return DrainResult(successCount = success, failureCount = failure, skippedCount = skipped)
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
                splitWithTravelerIds = payload.splitWithTravelerIds,
            ),
            idempotencyKey = entry.idempotencyKey,
        )
        purchaseLocalStore.replaceId(oldId = payload.localId, purchase = response.toDomain(pendingSync = false))
    }

    private suspend fun drainUpdatePurchase(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeUpdatePurchase(entry.payloadJson)
        val response = purchaseApi.updatePurchase(
            tripId = payload.tripId,
            purchaseId = payload.purchaseId,
            request = com.example.shoptourr.data.remote.dto.purchase.UpdatePurchaseRequest(
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
                splitWithTravelerIds = payload.splitWithTravelerIds,
            ),
        )
        purchaseLocalStore.upsert(response.toDomain(pendingSync = false))
    }

    private suspend fun drainDeletePurchase(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeDeletePurchase(entry.payloadJson)
        purchaseApi.deletePurchase(tripId = payload.tripId, purchaseId = payload.purchaseId)
        purchaseLocalStore.remove(payload.purchaseId)
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
                quoteCurrency = payload.quoteCurrency,
                travelers = payload.travelers.map {
                    CreateTravelerRequest(
                        name = it.name,
                        colorHex = it.colorHex,
                        avatarGlyph = it.avatarGlyph,
                    )
                }.ifEmpty { null },
            ),
            idempotencyKey = entry.idempotencyKey,
        )
        val serverTrip = response.toSummaryDomain()
        val replaced = tripLocalStore.all().map { if (it.id == payload.localId) serverTrip else it }
        tripLocalStore.replaceAll(replaced)
    }

    private suspend fun drainUpdateTrip(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeUpdateTrip(entry.payloadJson)
        val response = tripApi.updateTrip(
            tripId = payload.tripId,
            request = com.example.shoptourr.data.remote.dto.trip.UpdateTripRequest(
                city = payload.city,
                country = payload.country,
                countryCode = payload.countryCode,
                startDate = payload.startDate,
                endDate = payload.endDate,
                budget = payload.budgetAmount?.let { MoneyDto(it, payload.budgetCurrency ?: "EUR") },
                defaultVatRatePercent = payload.defaultVatRatePercent,
                status = payload.status?.let {
                    com.example.shoptourr.data.remote.dto.trip.TripStatus.valueOf(it)
                },
            ),
        )
        tripLocalStore.upsert(response.toSummaryDomain())
    }

    private suspend fun drainDeleteTrip(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeDeleteTrip(entry.payloadJson)
        tripApi.deleteTrip(payload.tripId)
        tripLocalStore.remove(payload.tripId)
    }

    private suspend fun drainCreateWishlist(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeWishlist(entry.payloadJson)
        val response = wishlistApi.create(
            request = CreateWishlistItemRequest(
                name = payload.name,
                city = payload.city,
                targetPrice = MoneyDto(payload.targetAmount, payload.targetCurrency),
                iconEmoji = payload.iconEmoji,
                note = payload.note,
            ),
            idempotencyKey = entry.idempotencyKey,
        )
        wishlistLocalStore.replaceId(
            oldId = payload.localId,
            item = WishlistItem(
                id = response.id,
                name = response.name,
                city = response.city,
                targetPrice = Money.parse(response.targetPrice.amount, response.targetPrice.currency),
                iconEmoji = response.iconEmoji,
                note = response.note,
                createdAt = response.createdAt,
            ),
        )
    }

    private suspend fun drainCreateDiary(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeDiary(entry.payloadJson)
        val response = diaryApi.create(
            tripId = payload.tripId,
            request = CreateDiaryEntryRequest(
                entryDate = payload.entryDate,
                mood = payload.mood,
                text = payload.text,
            ),
            idempotencyKey = entry.idempotencyKey,
        )
        diaryLocalStore.replaceId(
            oldId = payload.localId,
            entry = DiaryEntry(
                id = response.id,
                tripId = response.tripId,
                entryDate = response.entryDate,
                mood = response.mood,
                text = response.text,
                createdAt = response.createdAt,
                updatedAt = response.updatedAt,
            ),
        )
    }

    private suspend fun drainDeleteWishlist(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeDeleteWishlist(entry.payloadJson)
        runCatching { wishlistApi.delete(payload.itemId) }
            .onFailure { error ->
                if (error !is AppError.NotFound) throw error
            }
        wishlistLocalStore.remove(payload.itemId)
    }

    private suspend fun drainDeleteDiary(entry: SyncOutboxEntry) {
        val payload = SyncPayloadCodec.decodeDeleteDiary(entry.payloadJson)
        runCatching { diaryApi.delete(payload.tripId, payload.entryId) }
            .onFailure { error ->
                if (error !is AppError.NotFound) throw error
            }
        diaryLocalStore.removeEntry(payload.tripId, payload.entryId)
    }

    /**
     * v1 conflict policy: server wins — refresh entity and replace local, then drop outbox row.
     */
    private suspend fun reconcileServerWins(entry: SyncOutboxEntry) {
        when (entry.type) {
            SyncMutationType.CREATE_PURCHASE -> {
                val payload = SyncPayloadCodec.decodePurchase(entry.payloadJson)
                purchaseLocalStore.getById(payload.localId)?.let {
                    purchaseLocalStore.upsert(it.copy(pendingSync = false))
                }
            }
            SyncMutationType.UPDATE_PURCHASE,
            SyncMutationType.DELETE_PURCHASE,
            -> {
                val tripId: String
                val purchaseId: String
                if (entry.type == SyncMutationType.UPDATE_PURCHASE) {
                    val payload = SyncPayloadCodec.decodeUpdatePurchase(entry.payloadJson)
                    tripId = payload.tripId
                    purchaseId = payload.purchaseId
                } else {
                    val payload = SyncPayloadCodec.decodeDeletePurchase(entry.payloadJson)
                    tripId = payload.tripId
                    purchaseId = payload.purchaseId
                }
                runCatching { purchaseApi.fetchPurchase(tripId, purchaseId) }
                    .onSuccess { purchaseLocalStore.upsert(it.toDomain(pendingSync = false)) }
                    .onFailure { error ->
                        if (error is AppError.NotFound) {
                            purchaseLocalStore.remove(purchaseId)
                        } else {
                            throw error
                        }
                    }
            }
            SyncMutationType.CREATE_TRIP -> {
                // Local id is unknown to server; drop pending flag until next home refresh.
                val payload = SyncPayloadCodec.decodeTrip(entry.payloadJson)
                tripLocalStore.all().firstOrNull { it.id == payload.localId }?.let {
                    tripLocalStore.upsert(it)
                }
            }
            SyncMutationType.UPDATE_TRIP,
            SyncMutationType.DELETE_TRIP,
            -> {
                val tripId = if (entry.type == SyncMutationType.UPDATE_TRIP) {
                    SyncPayloadCodec.decodeUpdateTrip(entry.payloadJson).tripId
                } else {
                    SyncPayloadCodec.decodeDeleteTrip(entry.payloadJson).tripId
                }
                runCatching { tripApi.fetchTrip(tripId) }
                    .onSuccess { tripLocalStore.upsert(it.toSummaryDomain()) }
                    .onFailure { error ->
                        if (error is AppError.NotFound) {
                            tripLocalStore.remove(tripId)
                        } else {
                            throw error
                        }
                    }
            }
            SyncMutationType.CREATE_WISHLIST,
            SyncMutationType.DELETE_WISHLIST,
            SyncMutationType.CREATE_DIARY,
            SyncMutationType.DELETE_DIARY,
            -> {
                if (entry.type == SyncMutationType.DELETE_WISHLIST) {
                    val payload = SyncPayloadCodec.decodeDeleteWishlist(entry.payloadJson)
                    wishlistLocalStore.remove(payload.itemId)
                } else if (entry.type == SyncMutationType.DELETE_DIARY) {
                    val payload = SyncPayloadCodec.decodeDeleteDiary(entry.payloadJson)
                    diaryLocalStore.removeEntry(payload.tripId, payload.entryId)
                }
            }
        }
    }
}

private fun com.example.shoptourr.data.remote.dto.purchase.PurchaseDto.toDomain(
    pendingSync: Boolean,
): Purchase {
    val amountMoney = Money.parse(amount.amount, amount.currency)
    val vatBreakdown = VatCalculator.breakdown(
        amount = amountMoney,
        vatRatePercent = vat.vatRatePercent,
        vatIncluded = vat.vatIncluded,
    )
    return Purchase(
        id = id,
        tripId = tripId,
        name = name,
        category = PurchaseCategory.valueOf(category.name),
        amount = amountMoney,
        vat = vatBreakdown,
        taxRefundEligible = taxRefundEligible,
        place = place,
        purchaseDate = purchaseDate,
        purchaseTime = purchaseTime,
        pendingSync = pendingSync,
    )
}

private fun com.example.shoptourr.data.remote.dto.trip.TripDto.toSummaryDomain(): TripSummary =
    TripSummary(
        id = id,
        city = city,
        country = country,
        status = TripStatus.valueOf(status.name),
        startDate = startDate,
        endDate = endDate,
        budget = Money.parse(budget.amount, budget.currency),
        spent = Money.parse(spent.amount, spent.currency),
        purchaseCount = purchaseCount,
        flagEmoji = flagEmoji,
        datesLabel = datesLabel,
        currentDayNumber = currentDayNumber,
        dayCount = dayCount,
    )
