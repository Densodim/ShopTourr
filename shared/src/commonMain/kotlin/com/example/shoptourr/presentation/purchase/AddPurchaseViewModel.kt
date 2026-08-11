package com.example.shoptourr.presentation.purchase

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.PurchaseSplitCalculator
import com.example.shoptourr.domain.model.ReceiptOcrResult
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.FetchReceiptOcrUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class AddPurchaseUiState(
    val tripId: String,
    val name: String = "",
    val amount: String = "",
    val currency: String = "EUR",
    val category: PurchaseCategory = PurchaseCategory.FOOD,
    val place: String = "",
    val vatIncluded: Boolean = true,
    val vatRatePercent: String = "23",
    val taxRefundEligible: Boolean = false,
    val receiptMediaId: String? = null,
    val ocr: ReceiptOcrResult? = null,
    val travelers: List<Traveler> = emptyList(),
    val selectedTravelerIds: List<String> = emptyList(),
    val isUploadingReceipt: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState {
    val yourShare: Money?
        get() {
            if (amount.isBlank()) return null
            val money = runCatching { Money.parse(amount, currency) }.getOrNull() ?: return null
            return PurchaseSplitCalculator.share(
                amount = money,
                participantCount = selectedTravelerIds.size.coerceAtLeast(1),
            )
        }
}

sealed interface AddPurchaseIntent {
    data class NameChanged(val value: String) : AddPurchaseIntent
    data class AmountChanged(val value: String) : AddPurchaseIntent
    data class CurrencyChanged(val value: String) : AddPurchaseIntent
    data class CategoryChanged(val value: PurchaseCategory) : AddPurchaseIntent
    data class PlaceChanged(val value: String) : AddPurchaseIntent
    data class VatIncludedChanged(val value: Boolean) : AddPurchaseIntent
    data class VatRateChanged(val value: String) : AddPurchaseIntent
    data class TaxRefundChanged(val value: Boolean) : AddPurchaseIntent
    data class ToggleTraveler(val travelerId: String) : AddPurchaseIntent
    data class AttachReceipt(val contentType: String, val bytes: ByteArray) : AddPurchaseIntent
    data object ApplyOcr : AddPurchaseIntent
    data object Submit : AddPurchaseIntent
}

sealed interface AddPurchaseUiEvent : UiEvent {
    data class Created(val purchaseId: String) : AddPurchaseUiEvent
}

class AddPurchaseViewModel(
    tripId: String,
    private val createPurchase: CreatePurchaseUseCase,
    private val uploadReceipt: UploadReceiptUseCase,
    private val fetchReceiptOcr: FetchReceiptOcrUseCase,
    private val observeTripDetail: ObserveTripDetailUseCase,
) : BaseViewModel<AddPurchaseUiState, AddPurchaseUiEvent>(AddPurchaseUiState(tripId = tripId)) {

    init {
        launch {
            observeTripDetail(tripId).collect { detail ->
                val travelers = detail?.trip?.travelers.orEmpty()
                updateState {
                    val selected = resolveSelected(selectedTravelerIds, travelers)
                    copy(travelers = travelers, selectedTravelerIds = selected)
                }
            }
        }
    }

    fun onIntent(intent: AddPurchaseIntent) {
        when (intent) {
            is AddPurchaseIntent.NameChanged -> updateState { copy(name = intent.value, error = null) }
            is AddPurchaseIntent.AmountChanged -> updateState { copy(amount = intent.value, error = null) }
            is AddPurchaseIntent.CurrencyChanged ->
                updateState { copy(currency = intent.value.uppercase(), error = null) }
            is AddPurchaseIntent.CategoryChanged ->
                updateState { copy(category = intent.value, error = null) }
            is AddPurchaseIntent.PlaceChanged -> updateState { copy(place = intent.value, error = null) }
            is AddPurchaseIntent.VatIncludedChanged ->
                updateState { copy(vatIncluded = intent.value, error = null) }
            is AddPurchaseIntent.VatRateChanged ->
                updateState { copy(vatRatePercent = intent.value, error = null) }
            is AddPurchaseIntent.TaxRefundChanged ->
                updateState { copy(taxRefundEligible = intent.value, error = null) }
            is AddPurchaseIntent.ToggleTraveler -> toggleTraveler(intent.travelerId)
            is AddPurchaseIntent.AttachReceipt -> attachReceipt(intent.contentType, intent.bytes)
            AddPurchaseIntent.ApplyOcr -> applyOcr()
            AddPurchaseIntent.Submit -> submit()
        }
    }

    private fun toggleTraveler(travelerId: String) {
        updateState {
            val next = if (travelerId in selectedTravelerIds) {
                selectedTravelerIds - travelerId
            } else {
                selectedTravelerIds + travelerId
            }
            if (next.isEmpty()) this else copy(selectedTravelerIds = next, error = null)
        }
    }

    private fun attachReceipt(contentType: String, bytes: ByteArray) {
        launch {
            updateState { copy(isUploadingReceipt = true, error = null) }
            uploadReceipt(ReceiptUploadDraft(contentType = contentType, bytes = bytes))
                .onSuccess { asset ->
                    updateState {
                        copy(
                            isUploadingReceipt = false,
                            receiptMediaId = asset.id,
                            error = null,
                        )
                    }
                    fetchReceiptOcr(asset.id)
                        .onSuccess { ocr -> updateState { copy(ocr = ocr) } }
                        .onFailure { /* OCR is optional assist */ }
                }
                .onFailure { throwable ->
                    updateState {
                        copy(
                            isUploadingReceipt = false,
                            error = throwable.asAppError().toUiError(),
                        )
                    }
                }
        }
    }

    private fun applyOcr() {
        val ocr = state.value.ocr ?: return
        updateState {
            copy(
                name = ocr.suggestedName ?: name,
                amount = ocr.suggestedAmount ?: amount,
                place = ocr.suggestedPlace ?: place,
                category = ocr.suggestedCategory ?: category,
                error = null,
            )
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            val amount = runCatching {
                Money.parse(current.amount.ifBlank { "0" }, current.currency)
            }.getOrElse {
                updateState {
                    copy(
                        isLoading = false,
                        error = AppErrorValidation("amount").toUiError(),
                    )
                }
                return@launch
            }
            val draft = PurchaseDraft(
                name = current.name,
                category = current.category,
                amount = amount,
                vatIncluded = current.vatIncluded,
                vatRatePercent = current.vatRatePercent,
                place = current.place.ifBlank { null },
                taxRefundEligible = current.taxRefundEligible,
                receiptMediaId = current.receiptMediaId,
                splitWithTravelerIds = current.selectedTravelerIds,
            )
            createPurchase(current.tripId, draft)
                .onSuccess { purchase ->
                    updateState { copy(isLoading = false, error = null) }
                    emitEvent(AddPurchaseUiEvent.Created(purchase.id))
                }
                .onFailure { throwable ->
                    updateState {
                        copy(isLoading = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }

    private companion object {
        fun resolveSelected(
            current: List<String>,
            travelers: List<Traveler>,
        ): List<String> {
            if (travelers.isEmpty()) return emptyList()
            if (current.isEmpty()) {
                return listOf(
                    travelers.firstOrNull { it.isOwner }?.id
                        ?: travelers.first().id,
                )
            }
            val kept = current.filter { id -> travelers.any { it.id == id } }
            return kept.ifEmpty {
                listOf(travelers.firstOrNull { it.isOwner }?.id ?: travelers.first().id)
            }
        }
    }
}

private fun AppErrorValidation(message: String) =
    com.example.shoptourr.domain.error.AppError.Validation(message)
