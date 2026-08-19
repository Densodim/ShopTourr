package com.example.shoptourr.presentation.privacy

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.usecase.DeleteAccountUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class PrivacyUiState(
    val isWorking: Boolean = false,
    val confirmDelete: Boolean = false,
    val error: UiError? = null,
) : UiState

sealed interface PrivacyIntent {
    data object Back : PrivacyIntent
    data object RequestDeleteAccount : PrivacyIntent
    data object ConfirmDeleteAccount : PrivacyIntent
    data object CancelDelete : PrivacyIntent
}

sealed interface PrivacyUiEvent : UiEvent {
    data object NavigateBack : PrivacyUiEvent
    data object AccountDeleted : PrivacyUiEvent
}

class PrivacyViewModel(
    private val deleteAccount: DeleteAccountUseCase,
) : BaseViewModel<PrivacyUiState, PrivacyUiEvent>(PrivacyUiState()) {

    fun onIntent(intent: PrivacyIntent) {
        when (intent) {
            PrivacyIntent.Back -> emitEvent(PrivacyUiEvent.NavigateBack)
            PrivacyIntent.RequestDeleteAccount ->
                updateState { copy(confirmDelete = true, error = null) }
            PrivacyIntent.CancelDelete ->
                updateState { copy(confirmDelete = false, error = null) }
            PrivacyIntent.ConfirmDeleteAccount -> delete()
        }
    }

    private fun delete() {
        launch {
            updateState { copy(isWorking = true, error = null) }
            deleteAccount()
                .onSuccess {
                    updateState { copy(isWorking = false, confirmDelete = false) }
                    emitEvent(PrivacyUiEvent.AccountDeleted)
                }
                .onFailure { throwable ->
                    updateState {
                        copy(
                            isWorking = false,
                            error = throwable.asAppError().toUiError(),
                        )
                    }
                }
        }
    }
}
