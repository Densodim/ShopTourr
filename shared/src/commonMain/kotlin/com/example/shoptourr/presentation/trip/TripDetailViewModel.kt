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
    data object Back : TripDetailIntent
}

sealed interface TripDetailUiEvent : UiEvent {
    data class NavigateAddPurchase(val tripId: String) : TripDetailUiEvent
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
        when (intent) {
            TripDetailIntent.AddPurchase ->
                emitEvent(TripDetailUiEvent.NavigateAddPurchase(state.value.tripId))
            TripDetailIntent.Back -> emitEvent(TripDetailUiEvent.NavigateBack)
        }
    }
}
