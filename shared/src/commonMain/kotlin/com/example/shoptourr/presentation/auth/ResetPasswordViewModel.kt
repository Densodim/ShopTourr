package com.example.shoptourr.presentation.auth

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.usecase.ResetPasswordUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val token: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val done: Boolean = false,
    val error: UiError? = null,
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
                updateState { copy(email = intent.value, error = null) }
            is ResetPasswordIntent.TokenChanged ->
                updateState { copy(token = intent.value, error = null) }
            is ResetPasswordIntent.PasswordChanged ->
                updateState { copy(password = intent.value, error = null) }
            ResetPasswordIntent.Submit -> submit()
            ResetPasswordIntent.Finish -> emitEvent(ResetPasswordUiEvent.NavigateToSignIn)
            ResetPasswordIntent.Back -> emitEvent(ResetPasswordUiEvent.NavigateBack)
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            resetPassword(current.email, current.token, current.password)
                .onSuccess { updateState { copy(isLoading = false, done = true) } }
                .onFailure { throwable ->
                    updateState {
                        copy(isLoading = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }
}
