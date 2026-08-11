package com.example.shoptourr.presentation.taxfree

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.usecase.ObserveTaxFreeUseCase
import com.example.shoptourr.domain.usecase.RefreshTaxFreeUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TaxFreeUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val summary: TaxFreeSummary? = null,
    val error: UiError? = null,
) : UiState

sealed interface TaxFreeIntent {
    data object Refresh : TaxFreeIntent
    data object Back : TaxFreeIntent
}

sealed interface TaxFreeUiEvent : UiEvent {
    data object NavigateBack : TaxFreeUiEvent
    data object Logout : TaxFreeUiEvent
}

class TaxFreeViewModel(
    tripId: String,
    private val observeTaxFree: ObserveTaxFreeUseCase,
    private val refreshTaxFree: RefreshTaxFreeUseCase,
) : BaseViewModel<TaxFreeUiState, TaxFreeUiEvent>(TaxFreeUiState(tripId = tripId)) {

    init {
        launch {
            observeTaxFree(state.value.tripId).collectLatest { summary ->
                updateState { copy(summary = summary, isLoading = false) }
            }
        }
        onIntent(TaxFreeIntent.Refresh)
    }

    fun onIntent(intent: TaxFreeIntent) {
        when (intent) {
            TaxFreeIntent.Refresh -> refresh()
            TaxFreeIntent.Back -> emitEvent(TaxFreeUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshTaxFree(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) emitEvent(TaxFreeUiEvent.Logout)
                }
        }
    }
}
