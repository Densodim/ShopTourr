package com.example.shoptourr.presentation.auth

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val user: User? = null,
) : UiState

sealed interface AuthIntent {
    data class SubmitLogin(val email: String, val password: String) : AuthIntent
}

sealed interface AuthUiEvent : UiEvent {
    data object NavigateHome : AuthUiEvent
    data object Logout : AuthUiEvent
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
) : BaseViewModel<AuthUiState, AuthUiEvent>(AuthUiState()) {

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.SubmitLogin -> login(intent.email, intent.password)
        }
    }

    private fun login(email: String, password: String) {
        launch {
            updateState { copy(isLoading = true, error = null) }
            loginUseCase(email, password)
                .onSuccess { session ->
                    updateState { copy(isLoading = false, user = session.user, error = null) }
                    emitEvent(AuthUiEvent.NavigateHome)
                }
                .onFailure { throwable ->
                    val uiError = throwable.asAppError().toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is com.example.shoptourr.presentation.error.UiErrorAction.Logout) {
                        emitEvent(AuthUiEvent.Logout)
                    }
                }
        }
    }
}
