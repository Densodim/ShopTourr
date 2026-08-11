package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePurchaseRepository : PurchaseRepository {
    var createCalls: Int = 0
        private set
    var enqueuedSyncCalls: Int = 0
        private set

    private val items = MutableStateFlow<List<Purchase>>(emptyList())

    override suspend fun create(tripId: String, draft: PurchaseDraft): Result<Purchase> {
        createCalls += 1
        enqueuedSyncCalls += 1
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

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        items.map { list -> list.filter { it.tripId == tripId } }
}
