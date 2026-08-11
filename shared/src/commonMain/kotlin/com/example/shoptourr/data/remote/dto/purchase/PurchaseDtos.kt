package com.example.shoptourr.data.remote.dto.purchase

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.common.VatBreakdownDto
import kotlinx.serialization.Serializable

@Serializable
enum class PurchaseCategory {
    FOOD,
    TRANSPORT,
    SOUVENIRS,
    HOTEL,
    CULTURE,
    OTHER,
}

@Serializable
data class SplitShareDto(
    val travelerId: String,
    val travelerName: String,
    val share: MoneyDto,
)

@Serializable
data class PurchaseDto(
    val id: String,
    val tripId: String,
    val name: String,
    val category: PurchaseCategory,
    val amount: MoneyDto,
    val vat: VatBreakdownDto,
    val taxRefundEligible: Boolean,
    val place: String? = null,
    val purchaseDate: String,
    val purchaseTime: String? = null,
    val receiptMediaId: String? = null,
    val receiptThumbnailUrl: String? = null,
    val splitWithTravelerIds: List<String> = emptyList(),
    val splits: List<SplitShareDto> = emptyList(),
    val yourShare: MoneyDto,
    val quoteEquivalent: MoneyDto? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreatePurchaseRequest(
    val name: String,
    val category: PurchaseCategory,
    val amount: MoneyDto,
    val vatIncluded: Boolean = true,
    val vatRatePercent: String? = null,
    val taxRefundEligible: Boolean = false,
    val place: String? = null,
    val purchaseDate: String? = null,
    val purchaseTime: String? = null,
    val receiptMediaId: String? = null,
    val splitWithTravelerIds: List<String>? = null,
)

@Serializable
data class UpdatePurchaseRequest(
    val name: String? = null,
    val category: PurchaseCategory? = null,
    val amount: MoneyDto? = null,
    val vatIncluded: Boolean? = null,
    val vatRatePercent: String? = null,
    val taxRefundEligible: Boolean? = null,
    val place: String? = null,
    val purchaseDate: String? = null,
    val purchaseTime: String? = null,
    val receiptMediaId: String? = null,
    val splitWithTravelerIds: List<String>? = null,
)

@Serializable
data class PurchaseDayGroupDto(
    val date: String,
    val labelKey: String? = null,
    val dayTotal: MoneyDto,
    val items: List<PurchaseDto>,
)

@Serializable
data class TripPurchasesResponse(
    val spentTotal: MoneyDto,
    val budget: MoneyDto,
    val remaining: MoneyDto,
    val days: List<PurchaseDayGroupDto>,
)
