package com.example.shoptourr.presentation.profile

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.usecase.ActivatePremiumUseCase
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.domain.usecase.ObservePreferencesUseCase
import com.example.shoptourr.domain.usecase.ObserveProfileUseCase
import com.example.shoptourr.domain.usecase.RefreshPreferencesUseCase
import com.example.shoptourr.domain.usecase.RefreshProfileUseCase
import com.example.shoptourr.domain.usecase.UpdatePreferencesUseCase
import com.example.shoptourr.domain.usecase.UpdateProfileUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileFieldErrors(
    val displayName: String? = null,
    val currency: String? = null,
    val locale: String? = null,
)

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val preferences: UserPreferences? = null,
    val displayNameDraft: String = "",
    val currencyDraft: String = "",
    val localeDraft: String = "",
    val themeDraft: ThemeMode = ThemeMode.SYSTEM,
    val pushDraft: Boolean = true,
    val error: UiError? = null,
    val fieldErrors: ProfileFieldErrors = ProfileFieldErrors(),
) : UiState

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data class DisplayNameChanged(val value: String) : ProfileIntent
    data class CurrencyChanged(val value: String) : ProfileIntent
    data class LocaleChanged(val value: String) : ProfileIntent
    data class ThemeChanged(val value: ThemeMode) : ProfileIntent
    data class PushChanged(val value: Boolean) : ProfileIntent
    data object SaveProfile : ProfileIntent
    data object SavePreferences : ProfileIntent
    data object ActivatePlus : ProfileIntent
    data object Logout : ProfileIntent
    data object Back : ProfileIntent
}

sealed interface ProfileUiEvent : UiEvent {
    data object NavigateBack : ProfileUiEvent
    data object LoggedOut : ProfileUiEvent
}

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val observePreferences: ObservePreferencesUseCase,
    private val refreshProfile: RefreshProfileUseCase,
    private val refreshPreferences: RefreshPreferencesUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val updatePreferences: UpdatePreferencesUseCase,
    private val activatePremium: ActivatePremiumUseCase,
    private val logout: LogoutUseCase,
) : BaseViewModel<ProfileUiState, ProfileUiEvent>(ProfileUiState()) {

    init {
        launch {
            observeProfile().collectLatest { profile ->
                updateState {
                    copy(
                        profile = profile,
                        displayNameDraft = profile?.displayName ?: displayNameDraft,
                        isLoading = false,
                    )
                }
            }
        }
        launch {
            observePreferences().collectLatest { prefs ->
                updateState {
                    copy(
                        preferences = prefs,
                        currencyDraft = prefs?.preferredCurrency ?: currencyDraft,
                        localeDraft = prefs?.locale ?: localeDraft,
                        themeDraft = prefs?.theme ?: themeDraft,
                        pushDraft = prefs?.pushNotificationsEnabled ?: pushDraft,
                        isLoading = false,
                    )
                }
            }
        }
        onIntent(ProfileIntent.Refresh)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> refresh()
            is ProfileIntent.DisplayNameChanged ->
                updateState {
                    copy(
                        displayNameDraft = intent.value,
                        error = null,
                        fieldErrors = fieldErrors.copy(displayName = null),
                    )
                }
            is ProfileIntent.CurrencyChanged ->
                updateState {
                    copy(
                        currencyDraft = intent.value.uppercase(),
                        error = null,
                        fieldErrors = fieldErrors.copy(currency = null),
                    )
                }
            is ProfileIntent.LocaleChanged ->
                updateState {
                    copy(localeDraft = intent.value, error = null, fieldErrors = fieldErrors.copy(locale = null))
                }
            is ProfileIntent.ThemeChanged ->
                updateState { copy(themeDraft = intent.value, error = null) }
            is ProfileIntent.PushChanged ->
                updateState { copy(pushDraft = intent.value, error = null) }
            ProfileIntent.SaveProfile -> saveProfile()
            ProfileIntent.SavePreferences -> savePreferences()
            ProfileIntent.ActivatePlus -> activatePlus()
            ProfileIntent.Logout -> logoutUser()
            ProfileIntent.Back -> emitEvent(ProfileUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val profileResult = refreshProfile()
            val prefsResult = refreshPreferences()
            val failure = profileResult.exceptionOrNull() ?: prefsResult.exceptionOrNull()
            if (failure != null) {
                val uiError = failure.asAppError().toUiError()
                updateState { copy(isLoading = false, error = uiError) }
                if (uiError.action is UiErrorAction.Logout) {
                    emitEvent(ProfileUiEvent.LoggedOut)
                }
            } else {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun saveProfile() {
        launch {
            updateState { copy(isSaving = true, error = null) }
            val current = state.value
            updateProfile(UpdateProfileDraft(displayName = current.displayNameDraft))
                .onSuccess { updateState { copy(isSaving = false, fieldErrors = ProfileFieldErrors()) } }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isSaving = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = mapProfileField(fieldKey, current),
                        )
                    }
                }
        }
    }

    private fun savePreferences() {
        launch {
            val current = state.value
            updateState { copy(isSaving = true, error = null) }
            updatePreferences(
                UpdatePreferencesDraft(
                    locale = current.localeDraft.ifBlank { null },
                    preferredCurrency = current.currencyDraft.ifBlank { null },
                    theme = current.themeDraft,
                    pushNotificationsEnabled = current.pushDraft,
                    darkMode = current.themeDraft == ThemeMode.DARK,
                )
            )
                .onSuccess { updateState { copy(isSaving = false, fieldErrors = ProfileFieldErrors()) } }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isSaving = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = mapProfileField(fieldKey, current),
                        )
                    }
                }
        }
    }

    private fun mapProfileField(fieldKey: String?, state: ProfileUiState): ProfileFieldErrors =
        when (fieldKey) {
            "displayName" -> ProfileFieldErrors(
                displayName = if (state.displayNameDraft.isBlank()) {
                    "validation_person_name_required"
                } else {
                    "validation_name_invalid"
                },
            )
            "preferredCurrency" -> ProfileFieldErrors(currency = "validation_currency_invalid")
            "locale" -> ProfileFieldErrors(locale = "validation_locale_invalid")
            else -> ProfileFieldErrors()
        }

    private fun activatePlus() {
        launch {
            updateState { copy(isSaving = true, error = null) }
            activatePremium(PremiumPlan.PLUS)
                .onSuccess { updateState { copy(isSaving = false) } }
                .onFailure { throwable ->
                    updateState {
                        copy(isSaving = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }

    private fun logoutUser() {
        launch {
            updateState { copy(isSaving = true, error = null) }
            logout()
                .onSuccess {
                    updateState { copy(isSaving = false) }
                    emitEvent(ProfileUiEvent.LoggedOut)
                }
                .onFailure { throwable ->
                    updateState {
                        copy(isSaving = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }
}
