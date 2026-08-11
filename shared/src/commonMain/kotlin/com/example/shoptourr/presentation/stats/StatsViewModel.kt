package com.example.shoptourr.presentation.stats

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.usecase.ObserveStatsUseCase
import com.example.shoptourr.domain.usecase.RefreshStatsUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class StatsUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val stats: TripStats? = null,
    val error: UiError? = null,
) : UiState

sealed interface StatsIntent {
    data object Refresh : StatsIntent
    data object Back : StatsIntent
}

sealed interface StatsUiEvent : UiEvent {
    data object NavigateBack : StatsUiEvent
    data object Logout : StatsUiEvent
}

class StatsViewModel(
    tripId: String,
    private val observeStats: ObserveStatsUseCase,
    private val refreshStats: RefreshStatsUseCase,
) : BaseViewModel<StatsUiState, StatsUiEvent>(StatsUiState(tripId = tripId)) {

    init {
        launch {
            observeStats(state.value.tripId).collectLatest { stats ->
                updateState { copy(stats = stats, isLoading = false) }
            }
        }
        onIntent(StatsIntent.Refresh)
    }

    fun onIntent(intent: StatsIntent) {
        when (intent) {
            StatsIntent.Refresh -> refresh()
            StatsIntent.Back -> emitEvent(StatsUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshStats(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) emitEvent(StatsUiEvent.Logout)
                }
        }
    }
}
