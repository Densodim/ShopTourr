package com.example.shoptourr.presentation.home

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.usecase.AcknowledgeSyncConflictUseCase
import com.example.shoptourr.domain.usecase.ObserveConnectivityUseCase
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.ObserveSyncConflictsUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val snapshot: HomeSnapshot? = null,
    val conflictBanner: Boolean = false,
    val error: UiError? = null,
) : UiState {
    val showContent: Boolean get() = !isLoading && error == null && snapshot != null
}

sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data object DismissConflict : HomeIntent
}

sealed interface HomeUiEvent : UiEvent {
    data class ShowMessage(val message: String) : HomeUiEvent
    data object Logout : HomeUiEvent
}

class HomeViewModel(
    private val observeHome: ObserveHomeUseCase,
    private val refreshHome: RefreshHomeUseCase,
    private val observeConnectivity: ObserveConnectivityUseCase,
    private val observeSyncConflicts: ObserveSyncConflictsUseCase? = null,
    private val acknowledgeSyncConflict: AcknowledgeSyncConflictUseCase? = null,
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState()) {

    init {
        launch {
            observeConnectivity().collectLatest { online ->
                updateState { copy(isOnline = online) }
            }
        }
        observeSyncConflicts?.let { observe ->
            launch {
                observe().collectLatest { visible ->
                    updateState { copy(conflictBanner = visible) }
                }
            }
        }
        launch {
            observeHome().collectLatest { snapshot ->
                updateState { copy(isLoading = false, snapshot = snapshot, error = null) }
            }
        }
        onIntent(HomeIntent.Refresh)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> refresh()
            HomeIntent.DismissConflict -> acknowledgeSyncConflict?.invoke()
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isRefreshing = true, error = null) }
            refreshHome()
                .onSuccess { updateState { copy(isLoading = false, isRefreshing = false) } }
                .onFailure { error ->
                    val uiError = error.asAppError().toUiError()
                    updateState { copy(isLoading = false, isRefreshing = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) {
                        emitEvent(HomeUiEvent.Logout)
                    }
                }
        }
    }
}
