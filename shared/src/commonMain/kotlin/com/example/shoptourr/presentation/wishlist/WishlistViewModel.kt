package com.example.shoptourr.presentation.wishlist

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.ObserveWishlistUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.domain.validation.FieldRules
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WishlistFieldErrors(
    val name: String? = null,
    val city: String? = null,
    val price: String? = null,
) {
    val hasErrors: Boolean get() = name != null || city != null || price != null
}

data class WishlistUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    val nameDraft: String = "",
    val cityDraft: String = "",
    val priceDraft: String = "",
    val currencyDraft: String = "EUR",
    val fieldErrors: WishlistFieldErrors = WishlistFieldErrors(),
    val error: UiError? = null,
) : UiState

sealed interface WishlistIntent {
    data object Refresh : WishlistIntent
    data class NameChanged(val value: String) : WishlistIntent
    data class CityChanged(val value: String) : WishlistIntent
    data class PriceChanged(val value: String) : WishlistIntent
    data class CurrencyChanged(val value: String) : WishlistIntent
    data object Add : WishlistIntent
    data class Delete(val id: String) : WishlistIntent
    data object Back : WishlistIntent
}

sealed interface WishlistUiEvent : UiEvent {
    data object NavigateBack : WishlistUiEvent
    data object Logout : WishlistUiEvent
}

class WishlistViewModel(
    private val observeWishlist: ObserveWishlistUseCase,
    private val refreshWishlist: RefreshWishlistUseCase,
    private val createItem: CreateWishlistItemUseCase,
    private val deleteItem: DeleteWishlistItemUseCase,
) : BaseViewModel<WishlistUiState, WishlistUiEvent>(WishlistUiState()) {

    init {
        launch {
            observeWishlist().collectLatest { items ->
                updateState { copy(items = items, isLoading = false) }
            }
        }
        onIntent(WishlistIntent.Refresh)
    }

    fun onIntent(intent: WishlistIntent) {
        when (intent) {
            WishlistIntent.Refresh -> refresh()
            is WishlistIntent.NameChanged -> updateState {
                copy(
                    nameDraft = intent.value,
                    error = null,
                    fieldErrors = fieldErrors.copy(name = null),
                )
            }
            is WishlistIntent.CityChanged -> updateState {
                copy(
                    cityDraft = intent.value,
                    error = null,
                    fieldErrors = fieldErrors.copy(city = null),
                )
            }
            is WishlistIntent.PriceChanged -> updateState {
                copy(
                    priceDraft = intent.value,
                    error = null,
                    fieldErrors = fieldErrors.copy(price = null),
                )
            }
            is WishlistIntent.CurrencyChanged ->
                updateState {
                    copy(
                        currencyDraft = intent.value.uppercase(),
                        error = null,
                        fieldErrors = fieldErrors.copy(price = null),
                    )
                }
            WishlistIntent.Add -> add()
            is WishlistIntent.Delete -> remove(intent.id)
            WishlistIntent.Back -> emitEvent(WishlistUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshWishlist()
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) {
                        emitEvent(WishlistUiEvent.Logout)
                    }
                }
        }
    }

    private fun add() {
        val current = state.value
        val fieldErrors = validateFields(current)
        if (fieldErrors.hasErrors) {
            updateState { copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        launch {
            updateState { copy(isSaving = true, error = null) }
            val amount = Money.parse(current.priceDraft, current.currencyDraft)
            createItem(
                CreateWishlistDraft(
                    name = current.nameDraft,
                    city = current.cityDraft,
                    targetPrice = amount,
                )
            )
                .onSuccess {
                    updateState {
                        copy(
                            isSaving = false,
                            nameDraft = "",
                            cityDraft = "",
                            priceDraft = "",
                            fieldErrors = WishlistFieldErrors(),
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isSaving = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = when (fieldKey) {
                                "name" -> fieldErrors.copy(name = "validation_name_invalid")
                                "city" -> fieldErrors.copy(city = "validation_city_invalid")
                                "targetPrice" -> fieldErrors.copy(price = "validation_amount_positive")
                                else -> fieldErrors
                            },
                        )
                    }
                }
        }
    }

    private fun remove(id: String) {
        launch {
            updateState { copy(isSaving = true, error = null) }
            deleteItem(id)
                .onSuccess { updateState { copy(isSaving = false) } }
                .onFailure { throwable ->
                    updateState {
                        copy(isSaving = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }

    private fun validateFields(state: WishlistUiState): WishlistFieldErrors {
        val name = when {
            state.nameDraft.trim().isEmpty() -> "validation_name_required"
            !FieldRules.isItemName(state.nameDraft.trim()) -> "validation_name_invalid"
            else -> null
        }
        val city = when {
            state.cityDraft.trim().isEmpty() -> "validation_city_required"
            !FieldRules.isPlaceName(state.cityDraft.trim()) -> "validation_city_invalid"
            else -> null
        }
        val price = when {
            state.priceDraft.isBlank() -> "validation_amount_required"
            else -> {
                val parsed = runCatching { Money.parse(state.priceDraft, state.currencyDraft) }.getOrNull()
                when {
                    parsed == null -> "validation_amount_invalid"
                    parsed.minorUnits <= 0 -> "validation_amount_positive"
                    else -> null
                }
            }
        }
        return WishlistFieldErrors(name = name, city = city, price = price)
    }
}
