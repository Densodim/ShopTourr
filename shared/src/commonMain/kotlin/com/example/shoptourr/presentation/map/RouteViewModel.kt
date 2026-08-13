package com.example.shoptourr.presentation.map

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.FeatureFlag
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.usecase.ObserveFeatureFlagUseCase
import com.example.shoptourr.domain.usecase.ObserveRouteUseCase
import com.example.shoptourr.domain.usecase.RefreshRouteUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RouteUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val nativeMapsEnabled: Boolean = false,
    val route: TripRoute? = null,
    val error: UiError? = null,
) : UiState

sealed interface RouteIntent {
    data object Refresh : RouteIntent
    data object Back : RouteIntent
}

sealed interface RouteUiEvent : UiEvent {
    data object NavigateBack : RouteUiEvent
    data object Logout : RouteUiEvent
}

class RouteViewModel(
    tripId: String,
    private val observeRoute: ObserveRouteUseCase,
    private val refreshRoute: RefreshRouteUseCase,
    private val observeFeatureFlag: ObserveFeatureFlagUseCase? = null,
) : BaseViewModel<RouteUiState, RouteUiEvent>(RouteUiState(tripId = tripId)) {

    init {
        observeFeatureFlag?.let { flags ->
            launch {
                flags(FeatureFlag.NATIVE_MAPS).collectLatest { enabled ->
                    updateState { copy(nativeMapsEnabled = enabled) }
                }
            }
        }
        launch {
            observeRoute(state.value.tripId).collectLatest { route ->
                updateState { copy(route = route, isLoading = false) }
            }
        }
        onIntent(RouteIntent.Refresh)
    }

    fun onIntent(intent: RouteIntent) {
        when (intent) {
            RouteIntent.Refresh -> refresh()
            RouteIntent.Back -> emitEvent(RouteUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshRoute(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) emitEvent(RouteUiEvent.Logout)
                }
        }
    }
}
