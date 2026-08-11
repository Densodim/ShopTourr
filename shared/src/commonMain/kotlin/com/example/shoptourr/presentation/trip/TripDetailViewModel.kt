package com.example.shoptourr.presentation.trip

import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val detail: TripDetail? = null,
    val error: UiError? = null,
) : UiState

sealed interface TripDetailIntent {
    data object AddPurchase : TripDetailIntent
    data object OpenDiary : TripDetailIntent
    data object OpenTaxFree : TripDetailIntent
    data object OpenAlerts : TripDetailIntent
    data object OpenMap : TripDetailIntent
    data object OpenStats : TripDetailIntent
    data object Back : TripDetailIntent
}

sealed interface TripDetailUiEvent : UiEvent {
    data class NavigateAddPurchase(val tripId: String) : TripDetailUiEvent
    data class NavigateDiary(val tripId: String) : TripDetailUiEvent
    data class NavigateTaxFree(val tripId: String) : TripDetailUiEvent
    data class NavigateAlerts(val tripId: String) : TripDetailUiEvent
    data class NavigateMap(val tripId: String) : TripDetailUiEvent
    data class NavigateStats(val tripId: String) : TripDetailUiEvent
    data object NavigateBack : TripDetailUiEvent
}

class TripDetailViewModel(
    tripId: String,
    private val observeTripDetail: ObserveTripDetailUseCase,
) : BaseViewModel<TripDetailUiState, TripDetailUiEvent>(TripDetailUiState(tripId = tripId)) {

    init {
        launch {
            observeTripDetail(state.value.tripId).collectLatest { detail ->
                updateState {
                    copy(
                        isLoading = false,
                        detail = detail,
                        error = if (detail == null) {
                            UiError(
                                title = "Not Found",
                                message = "Trip was not found",
                                isRetryable = false,
                            )
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }

    fun onIntent(intent: TripDetailIntent) {
        val tripId = state.value.tripId
        when (intent) {
            TripDetailIntent.AddPurchase -> emitEvent(TripDetailUiEvent.NavigateAddPurchase(tripId))
            TripDetailIntent.OpenDiary -> emitEvent(TripDetailUiEvent.NavigateDiary(tripId))
            TripDetailIntent.OpenTaxFree -> emitEvent(TripDetailUiEvent.NavigateTaxFree(tripId))
            TripDetailIntent.OpenAlerts -> emitEvent(TripDetailUiEvent.NavigateAlerts(tripId))
            TripDetailIntent.OpenMap -> emitEvent(TripDetailUiEvent.NavigateMap(tripId))
            TripDetailIntent.OpenStats -> emitEvent(TripDetailUiEvent.NavigateStats(tripId))
            TripDetailIntent.Back -> emitEvent(TripDetailUiEvent.NavigateBack)
        }
    }
}
