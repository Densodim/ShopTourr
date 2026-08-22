package com.example.shoptourr.presentation.auth

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.usecase.ResetPasswordUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class ResetPasswordFieldErrors(
    val email: String? = null,
    val token: String? = null,
    val password: String? = null,
)

data class ResetPasswordUiState(
    val email: String = "",
    val token: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val done: Boolean = false,
    val error: UiError? = null,
    val fieldErrors: ResetPasswordFieldErrors = ResetPasswordFieldErrors(),
) : UiState

sealed interface ResetPasswordIntent {
    data class Prefill(val email: String, val token: String) : ResetPasswordIntent
    data class EmailChanged(val value: String) : ResetPasswordIntent
    data class TokenChanged(val value: String) : ResetPasswordIntent
    data class PasswordChanged(val value: String) : ResetPasswordIntent
    data object Submit : ResetPasswordIntent
    data object Finish : ResetPasswordIntent
    data object Back : ResetPasswordIntent
}

sealed interface ResetPasswordUiEvent : UiEvent {
    data object NavigateBack : ResetPasswordUiEvent
    data object NavigateToSignIn : ResetPasswordUiEvent
}

class ResetPasswordViewModel(
    private val resetPassword: ResetPasswordUseCase,
) : BaseViewModel<ResetPasswordUiState, ResetPasswordUiEvent>(ResetPasswordUiState()) {

    fun onIntent(intent: ResetPasswordIntent) {
        when (intent) {
            is ResetPasswordIntent.Prefill ->
                updateState {
                    copy(
                        email = intent.email.ifBlank { email },
                        token = intent.token.ifBlank { token },
                        error = null,
                    )
                }
            is ResetPasswordIntent.EmailChanged ->
                updateState { copy(email = intent.value, error = null, fieldErrors = fieldErrors.copy(email = null)) }
            is ResetPasswordIntent.TokenChanged ->
                updateState { copy(token = intent.value, error = null, fieldErrors = fieldErrors.copy(token = null)) }
            is ResetPasswordIntent.PasswordChanged ->
                updateState {
                    copy(password = intent.value, error = null, fieldErrors = fieldErrors.copy(password = null))
                }
            ResetPasswordIntent.Submit -> submit()
            ResetPasswordIntent.Finish -> emitEvent(ResetPasswordUiEvent.NavigateToSignIn)
            ResetPasswordIntent.Back -> emitEvent(ResetPasswordUiEvent.NavigateBack)
        }
    }

    private fun mapResetField(fieldKey: String?, state: ResetPasswordUiState): ResetPasswordFieldErrors =
        when (fieldKey) {
            "email" -> ResetPasswordFieldErrors(
                email = if (state.email.isBlank()) "validation_email_required" else "validation_email_invalid",
            )
            "token" -> ResetPasswordFieldErrors(
                token = if (state.token.isBlank()) "validation_token_required" else "validation_token_invalid",
            )
            "newPassword" -> ResetPasswordFieldErrors(
                password = if (state.password.isBlank()) {
                    "validation_password_required"
                } else {
                    "validation_password_short"
                },
            )
            else -> ResetPasswordFieldErrors()
        }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            resetPassword(current.email, current.token, current.password)
                .onSuccess {
                    updateState { copy(isLoading = false, done = true, fieldErrors = ResetPasswordFieldErrors()) }
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isLoading = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = mapResetField(fieldKey, current),
                        )
                    }
                }
        }
    }
}
