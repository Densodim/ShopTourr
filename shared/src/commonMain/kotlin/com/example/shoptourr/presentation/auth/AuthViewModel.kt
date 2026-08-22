package com.example.shoptourr.presentation.auth

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.domain.usecase.RegisterUseCase
import com.example.shoptourr.domain.usecase.SocialLoginUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class AuthFieldErrors(
    val displayName: String? = null,
    val email: String? = null,
    val password: String? = null,
)

data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val fieldErrors: AuthFieldErrors = AuthFieldErrors(),
    val user: User? = null,
) : UiState

sealed interface AuthIntent {
    data object ToggleMode : AuthIntent
    data class SetRegisterMode(val enabled: Boolean) : AuthIntent
    data class DisplayNameChanged(val value: String) : AuthIntent
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data object Submit : AuthIntent
    data class SocialSignIn(val provider: SocialProvider) : AuthIntent
}

sealed interface AuthUiEvent : UiEvent {
    data object NavigateHome : AuthUiEvent
    data object Logout : AuthUiEvent
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
) : BaseViewModel<AuthUiState, AuthUiEvent>(AuthUiState()) {

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.ToggleMode ->
                updateState { copy(isRegisterMode = !isRegisterMode, error = null, fieldErrors = AuthFieldErrors()) }
            is AuthIntent.SetRegisterMode ->
                updateState { copy(isRegisterMode = intent.enabled, error = null, fieldErrors = AuthFieldErrors()) }
            is AuthIntent.DisplayNameChanged ->
                updateState { copy(displayName = intent.value, error = null, fieldErrors = fieldErrors.copy(displayName = null)) }
            is AuthIntent.EmailChanged ->
                updateState { copy(email = intent.value, error = null, fieldErrors = fieldErrors.copy(email = null)) }
            is AuthIntent.PasswordChanged ->
                updateState { copy(password = intent.value, error = null, fieldErrors = fieldErrors.copy(password = null)) }
            AuthIntent.Submit -> submit()
            is AuthIntent.SocialSignIn -> socialSignIn(intent.provider)
        }
    }

    private fun mapAuthField(fieldKey: String?, state: AuthUiState): AuthFieldErrors = when (fieldKey) {
        "displayName" -> AuthFieldErrors(
            displayName = if (state.displayName.isBlank()) {
                "validation_person_name_required"
            } else {
                "validation_name_invalid"
            },
        )
        "email" -> AuthFieldErrors(
            email = if (state.email.isBlank()) "validation_email_required" else "validation_email_invalid",
        )
        "password" -> AuthFieldErrors(
            password = if (state.password.isBlank()) {
                "validation_password_required"
            } else {
                "validation_password_short"
            },
        )
        else -> AuthFieldErrors()
    }

    private fun socialSignIn(provider: SocialProvider) {
        launch {
            updateState { copy(isLoading = true, error = null) }
            socialLoginUseCase(provider)
                .onSuccess { session ->
                    updateState { copy(isLoading = false, user = session.user, error = null) }
                    emitEvent(AuthUiEvent.NavigateHome)
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    if (appError is AppError.Cancelled) {
                        updateState { copy(isLoading = false, error = null) }
                        return@onFailure
                    }
                    val uiError = appError.toUiError()
                    updateState { copy(isLoading = false, error = uiError) }
                    if (uiError.action is UiErrorAction.Logout) emitEvent(AuthUiEvent.Logout)
                }
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            val result = if (current.isRegisterMode) {
                registerUseCase(current.displayName, current.email, current.password)
            } else {
                loginUseCase(current.email, current.password)
            }
            result
                .onSuccess { session ->
                    updateState {
                        copy(isLoading = false, user = session.user, error = null, fieldErrors = AuthFieldErrors())
                    }
                    emitEvent(AuthUiEvent.NavigateHome)
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    val uiError = if (fieldKey == null) appError.toUiError() else null
                    updateState {
                        copy(
                            isLoading = false,
                            error = uiError,
                            fieldErrors = mapAuthField(fieldKey, current),
                        )
                    }
                    if (uiError?.action is UiErrorAction.Logout) emitEvent(AuthUiEvent.Logout)
                }
        }
    }
}
