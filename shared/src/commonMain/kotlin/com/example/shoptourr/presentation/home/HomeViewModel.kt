package com.example.shoptourr.presentation.home

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val snapshot: HomeSnapshot? = null,
    val error: AppError? = null,
) : UiState

sealed interface HomeIntent {
    data object Refresh : HomeIntent
}

sealed interface HomeUiEvent : UiEvent {
    data class ShowMessage(val message: String) : HomeUiEvent
}

class HomeViewModel(
    private val observeHome: ObserveHomeUseCase,
    private val refreshHome: RefreshHomeUseCase,
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState()) {

    init {
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
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshHome()
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { error ->
                    val appError = error as? AppError ?: AppError.Unknown(error.message)
                    updateState { copy(isLoading = false, error = appError) }
                }
        }
    }
}
