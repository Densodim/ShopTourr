package com.example.shoptourr.presentation.purchase

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
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
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState

sealed interface AddPurchaseIntent {
    data class NameChanged(val value: String) : AddPurchaseIntent
    data class AmountChanged(val value: String) : AddPurchaseIntent
    data class CurrencyChanged(val value: String) : AddPurchaseIntent
    data class CategoryChanged(val value: PurchaseCategory) : AddPurchaseIntent
    data class PlaceChanged(val value: String) : AddPurchaseIntent
    data class VatIncludedChanged(val value: Boolean) : AddPurchaseIntent
    data class VatRateChanged(val value: String) : AddPurchaseIntent
    data class TaxRefundChanged(val value: Boolean) : AddPurchaseIntent
    data object Submit : AddPurchaseIntent
}

sealed interface AddPurchaseUiEvent : UiEvent {
    data class Created(val purchaseId: String) : AddPurchaseUiEvent
}

class AddPurchaseViewModel(
    tripId: String,
    private val createPurchase: CreatePurchaseUseCase,
) : BaseViewModel<AddPurchaseUiState, AddPurchaseUiEvent>(AddPurchaseUiState(tripId = tripId)) {

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
            AddPurchaseIntent.Submit -> submit()
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
}

private fun AppErrorValidation(message: String) =
    com.example.shoptourr.domain.error.AppError.Validation(message)
