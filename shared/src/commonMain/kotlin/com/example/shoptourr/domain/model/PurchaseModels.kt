package com.example.shoptourr.domain.model

enum class PurchaseCategory {
    FOOD,
    TRANSPORT,
    SOUVENIRS,
    HOTEL,
    CULTURE,
    OTHER,
}

data class PurchaseDraft(
    val name: String,
    val category: PurchaseCategory,
    val amount: Money,
    val vatIncluded: Boolean,
    val vatRatePercent: String,
    val place: String?,
    val taxRefundEligible: Boolean = false,
    val purchaseDate: String? = null,
    val purchaseTime: String? = null,
    val receiptMediaId: String? = null,
    val splitWithTravelerIds: List<String> = emptyList(),
)

data class Purchase(
    val id: String,
    val tripId: String,
    val name: String,
    val category: PurchaseCategory,
    val amount: Money,
    val vat: VatBreakdown,
    val taxRefundEligible: Boolean,
    val place: String?,
    val purchaseDate: String,
    val purchaseTime: String?,
    val pendingSync: Boolean,
    val updatedAt: String? = null,
)
