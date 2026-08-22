package com.example.shoptourr.presentation.auth

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.usecase.RequestPasswordResetUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class ForgotPasswordFieldErrors(
    val email: String? = null,
)

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val error: UiError? = null,
    val fieldErrors: ForgotPasswordFieldErrors = ForgotPasswordFieldErrors(),
) : UiState

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val value: String) : ForgotPasswordIntent
    data object Submit : ForgotPasswordIntent
    data object Back : ForgotPasswordIntent
}

sealed interface ForgotPasswordUiEvent : UiEvent {
    data object NavigateBack : ForgotPasswordUiEvent
}

class ForgotPasswordViewModel(
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : BaseViewModel<ForgotPasswordUiState, ForgotPasswordUiEvent>(ForgotPasswordUiState()) {

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged ->
                updateState { copy(email = intent.value, error = null, fieldErrors = fieldErrors.copy(email = null)) }
            ForgotPasswordIntent.Submit -> submit()
            ForgotPasswordIntent.Back -> emitEvent(ForgotPasswordUiEvent.NavigateBack)
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            requestPasswordReset(current.email)
                .onSuccess {
                    updateState { copy(isLoading = false, sent = true, fieldErrors = ForgotPasswordFieldErrors()) }
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isLoading = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = if (fieldKey == "email") {
                                ForgotPasswordFieldErrors(
                                    email = if (current.email.isBlank()) {
                                        "validation_email_required"
                                    } else {
                                        "validation_email_invalid"
                                    },
                                )
                            } else {
                                ForgotPasswordFieldErrors()
                            },
                        )
                    }
                }
        }
    }
}
