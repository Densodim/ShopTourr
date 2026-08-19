package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.PurchasePageKeyset
import com.example.shoptourr.domain.model.PurchasePageRequest
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePurchaseRepository(
    private val createError: Throwable? = null,
) : PurchaseRepository {
    var createCalls: Int = 0
        private set
    var enqueuedSyncCalls: Int = 0
        private set
    var lastDraft: PurchaseDraft? = null
        private set
    var lastRefreshRequest: PurchasePageRequest? = null
        private set
    val refreshRequests: MutableList<PurchasePageRequest> = mutableListOf()
    var refreshPageCalls: Int = 0
        private set

    private val items = MutableStateFlow<List<Purchase>>(emptyList())

    override suspend fun create(tripId: String, draft: PurchaseDraft): Result<Purchase> {
        createError?.let { return Result.failure(it) }
        createCalls += 1
        enqueuedSyncCalls += 1
        lastDraft = draft
        val vat = VatCalculator.breakdown(draft.amount, draft.vatRatePercent, draft.vatIncluded)
        val purchase = Purchase(
            id = "p-$createCalls",
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
        items.update { it + purchase }
        return Result.success(purchase)
    }

    override suspend fun update(
        tripId: String,
        purchaseId: String,
        draft: PurchaseDraft,
    ): Result<Purchase> {
        val vat = VatCalculator.breakdown(draft.amount, draft.vatRatePercent, draft.vatIncluded)
        val purchase = Purchase(
            id = purchaseId,
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
        items.update { list -> list.map { if (it.id == purchaseId) purchase else it } }
        return Result.success(purchase)
    }

    override suspend fun delete(tripId: String, purchaseId: String): Result<Unit> {
        items.update { list -> list.filterNot { it.id == purchaseId } }
        return Result.success(Unit)
    }

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        items.map { list -> list.filter { it.tripId == tripId } }

    override suspend fun refreshPage(
        tripId: String,
        request: PurchasePageRequest,
    ): Result<List<Purchase>> {
        lastRefreshRequest = request
        refreshRequests += request
        refreshPageCalls += 1
        val forTrip = items.value.filter { it.tripId == tripId }
        return Result.success(PurchasePageKeyset.slice(forTrip, request))
    }
}
