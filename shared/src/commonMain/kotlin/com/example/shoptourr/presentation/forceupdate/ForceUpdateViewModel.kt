package com.example.shoptourr.presentation.forceupdate

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.ForceUpdateAction
import com.example.shoptourr.domain.model.ForceUpdateDecision
import com.example.shoptourr.domain.usecase.EvaluateForceUpdateUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class ForceUpdateUiState(
    val isLoading: Boolean = true,
    val action: ForceUpdateAction = ForceUpdateAction.NONE,
    val storeUrl: String? = null,
    val error: UiError? = null,
) : UiState

sealed interface ForceUpdateIntent {
    data object Refresh : ForceUpdateIntent
    data object DismissSoft : ForceUpdateIntent
}

sealed interface ForceUpdateUiEvent : UiEvent {
    data object SoftDismissed : ForceUpdateUiEvent
}

class ForceUpdateViewModel(
    private val evaluateForceUpdate: EvaluateForceUpdateUseCase,
) : BaseViewModel<ForceUpdateUiState, ForceUpdateUiEvent>(ForceUpdateUiState()) {

    init {
        onIntent(ForceUpdateIntent.Refresh)
    }

    fun onIntent(intent: ForceUpdateIntent) {
        when (intent) {
            ForceUpdateIntent.Refresh -> refresh()
            ForceUpdateIntent.DismissSoft -> {
                updateState { copy(action = ForceUpdateAction.NONE) }
                emitEvent(ForceUpdateUiEvent.SoftDismissed)
            }
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            evaluateForceUpdate()
                .onSuccess { decision: ForceUpdateDecision ->
                    updateState {
                        copy(
                            isLoading = false,
                            action = decision.action,
                            storeUrl = decision.storeUrl,
                        )
                    }
                }
                .onFailure { throwable ->
                    // Fail open: don't brick the app if config endpoint is down.
                    updateState {
                        copy(
                            isLoading = false,
                            action = ForceUpdateAction.NONE,
                            error = throwable.asAppError().toUiError(),
                        )
                    }
                }
        }
    }
}
