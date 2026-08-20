package com.example.shoptourr.presentation.lock

import com.example.shoptourr.domain.lock.AppLockStore
import com.example.shoptourr.domain.lock.BiometricAuthenticator
import com.example.shoptourr.domain.lock.BiometricAvailability
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.i18n.VoyageI18n
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import kotlinx.coroutines.launch

data class AppLockUiState(
    val enabled: Boolean = false,
    val locked: Boolean = false,
    val available: Boolean = false,
    val authenticating: Boolean = false,
    val error: UiError? = null,
) : UiState

sealed interface AppLockIntent {
    data object Bind : AppLockIntent
    data object AppStopped : AppLockIntent
    data object Unlock : AppLockIntent
    data class SetEnabled(val enabled: Boolean) : AppLockIntent
}

sealed interface AppLockUiEvent : UiEvent

class AppLockViewModel(
    private val store: AppLockStore,
    private val authenticator: BiometricAuthenticator,
    private val isLoggedIn: IsLoggedInUseCase,
) : BaseViewModel<AppLockUiState, AppLockUiEvent>(
    AppLockUiState(
        enabled = store.isEnabled(),
        locked = store.isEnabled() && isLoggedIn(),
    ),
) {
    private var promptOnStart: Boolean = store.isEnabled() && isLoggedIn()

    fun onIntent(intent: AppLockIntent) {
        when (intent) {
            AppLockIntent.Bind -> bind()
            AppLockIntent.AppStopped -> onAppStopped()
            AppLockIntent.Unlock -> unlock()
            is AppLockIntent.SetEnabled -> setEnabled(intent.enabled)
        }
    }

    private fun bind() {
        if (!isLoggedIn()) {
            promptOnStart = false
            updateState { copy(locked = false, authenticating = false) }
        }
        launch {
            val availability = authenticator.availability()
            updateState {
                copy(
                    available = availability == BiometricAvailability.AVAILABLE,
                    enabled = store.isEnabled(),
                )
            }
            if (!isLoggedIn()) return@launch
            if (store.isEnabled() && state.value.locked && promptOnStart && !state.value.authenticating) {
                unlock()
            }
        }
    }

    private fun onAppStopped() {
        if (state.value.authenticating) return
        if (store.isEnabled() && isLoggedIn()) {
            promptOnStart = true
            updateState { copy(locked = true) }
        }
    }

    private fun unlock() {
        if (!state.value.locked || state.value.authenticating) return
        promptOnStart = false
        launch {
            updateState { copy(authenticating = true, error = null) }
            val ok = authenticator.authenticate(lockReason())
            updateState { copy(authenticating = false, locked = !ok) }
        }
    }

    private fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            store.setEnabled(false)
            promptOnStart = false
            updateState { copy(enabled = false, locked = false, error = null) }
            return
        }
        if (state.value.authenticating) return
        launch {
            val availability = authenticator.availability()
            val available = availability == BiometricAvailability.AVAILABLE
            updateState { copy(available = available) }
            if (!available) {
                updateState {
                    copy(
                        error = UiError(
                            titleKey = "error_validation_title",
                            messageKey = if (availability == BiometricAvailability.NOT_ENROLLED) {
                                "lock_not_enrolled"
                            } else {
                                "lock_unavailable"
                            },
                            isRetryable = false,
                        ),
                    )
                }
                return@launch
            }
            updateState { copy(authenticating = true, error = null) }
            val ok = authenticator.authenticate(lockReason())
            updateState { copy(authenticating = false) }
            if (ok) {
                store.setEnabled(true)
                promptOnStart = false
                updateState { copy(enabled = true, locked = false) }
            }
        }
    }

    private fun lockReason(): String =
        VoyageI18n.t(VoyageI18n.currentLocale, "lock_reason")
}
