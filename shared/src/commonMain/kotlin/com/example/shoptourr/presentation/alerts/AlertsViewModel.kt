package com.example.shoptourr.presentation.alerts

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.usecase.ObserveAlertsUseCase
import com.example.shoptourr.domain.usecase.RefreshAlertsUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AlertsUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val alerts: List<BudgetAlert> = emptyList(),
    val error: UiError? = null,
) : UiState

sealed interface AlertsIntent {
    data object Refresh : AlertsIntent
    data object Back : AlertsIntent
}

sealed interface AlertsUiEvent : UiEvent {
    data object NavigateBack : AlertsUiEvent
    data object Logout : AlertsUiEvent
}

class AlertsViewModel(
    tripId: String,
    private val observeAlerts: ObserveAlertsUseCase,
    private val refreshAlerts: RefreshAlertsUseCase,
) : BaseViewModel<AlertsUiState, AlertsUiEvent>(AlertsUiState(tripId = tripId)) {

    init {
        launch {
            observeAlerts(state.value.tripId).collectLatest { alerts ->
                updateState { copy(alerts = alerts, isLoading = false) }
            }
        }
        onIntent(AlertsIntent.Refresh)
    }

    fun onIntent(intent: AlertsIntent) {
        when (intent) {
            AlertsIntent.Refresh -> refresh()
            AlertsIntent.Back -> emitEvent(AlertsUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshAlerts(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) emitEvent(AlertsUiEvent.Logout)
                }
        }
    }
}
