package com.example.shoptourr.presentation.wishlist

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.ObserveWishlistUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WishlistUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    val nameDraft: String = "",
    val cityDraft: String = "",
    val priceDraft: String = "",
    val currencyDraft: String = "EUR",
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
            is WishlistIntent.NameChanged -> updateState { copy(nameDraft = intent.value, error = null) }
            is WishlistIntent.CityChanged -> updateState { copy(cityDraft = intent.value, error = null) }
            is WishlistIntent.PriceChanged -> updateState { copy(priceDraft = intent.value, error = null) }
            is WishlistIntent.CurrencyChanged ->
                updateState { copy(currencyDraft = intent.value.uppercase(), error = null) }
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
        launch {
            val current = state.value
            updateState { copy(isSaving = true, error = null) }
            val amount = runCatching {
                Money.parse(current.priceDraft.ifBlank { "0" }, current.currencyDraft)
            }.getOrElse {
                updateState {
                    copy(
                        isSaving = false,
                        error = com.example.shoptourr.domain.error.AppError.Validation("targetPrice")
                            .toUiError(),
                    )
                }
                return@launch
            }
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
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    updateState {
                        copy(isSaving = false, error = throwable.asAppError().toUiError())
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
}
