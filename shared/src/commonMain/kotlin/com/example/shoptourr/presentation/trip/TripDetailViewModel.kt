package com.example.shoptourr.presentation.trip

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.domain.model.TripInvite
import com.example.shoptourr.domain.usecase.AddTravelerUseCase
import com.example.shoptourr.domain.usecase.InviteTravelerUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.RefreshExchangeRateUseCase
import com.example.shoptourr.domain.usecase.RefreshTripUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val detail: TripDetail? = null,
    val travelerNameDraft: String = "",
    val inviteEmailDraft: String = "",
    val lastInvite: TripInvite? = null,
    val error: UiError? = null,
) : UiState

sealed interface TripDetailIntent {
    data object Refresh : TripDetailIntent
    data object AddPurchase : TripDetailIntent
    data object OpenDiary : TripDetailIntent
    data object OpenTaxFree : TripDetailIntent
    data object OpenAlerts : TripDetailIntent
    data object OpenMap : TripDetailIntent
    data object OpenStats : TripDetailIntent
    data object OpenExport : TripDetailIntent
    data class TravelerNameChanged(val value: String) : TripDetailIntent
    data object AddTraveler : TripDetailIntent
    data class InviteEmailChanged(val value: String) : TripDetailIntent
    data object InviteTraveler : TripDetailIntent
    data object RefreshFx : TripDetailIntent
    data object Back : TripDetailIntent
}

sealed interface TripDetailUiEvent : UiEvent {
    data class NavigateAddPurchase(val tripId: String) : TripDetailUiEvent
    data class NavigateDiary(val tripId: String) : TripDetailUiEvent
    data class NavigateTaxFree(val tripId: String) : TripDetailUiEvent
    data class NavigateAlerts(val tripId: String) : TripDetailUiEvent
    data class NavigateMap(val tripId: String) : TripDetailUiEvent
    data class NavigateStats(val tripId: String) : TripDetailUiEvent
    data class NavigateExport(val tripId: String) : TripDetailUiEvent
    data object NavigateBack : TripDetailUiEvent
    data object Logout : TripDetailUiEvent
}

class TripDetailViewModel(
    tripId: String,
    private val observeTripDetail: ObserveTripDetailUseCase,
    private val refreshTrip: RefreshTripUseCase,
    private val addTraveler: AddTravelerUseCase,
    private val inviteTraveler: InviteTravelerUseCase,
    private val refreshExchangeRate: RefreshExchangeRateUseCase,
) : BaseViewModel<TripDetailUiState, TripDetailUiEvent>(TripDetailUiState(tripId = tripId)) {

    init {
        launch {
            observeTripDetail(state.value.tripId).collectLatest { detail ->
                updateState {
                    copy(
                        isLoading = false,
                        detail = detail,
                        error = if (detail == null) {
                            UiError(
                                title = "Not Found",
                                message = "Trip was not found",
                                isRetryable = false,
                            )
                        } else {
                            null
                        },
                    )
                }
            }
        }
        onIntent(TripDetailIntent.Refresh)
    }

    fun onIntent(intent: TripDetailIntent) {
        val tripId = state.value.tripId
        when (intent) {
            TripDetailIntent.Refresh -> refresh()
            TripDetailIntent.AddPurchase -> emitEvent(TripDetailUiEvent.NavigateAddPurchase(tripId))
            TripDetailIntent.OpenDiary -> emitEvent(TripDetailUiEvent.NavigateDiary(tripId))
            TripDetailIntent.OpenTaxFree -> emitEvent(TripDetailUiEvent.NavigateTaxFree(tripId))
            TripDetailIntent.OpenAlerts -> emitEvent(TripDetailUiEvent.NavigateAlerts(tripId))
            TripDetailIntent.OpenMap -> emitEvent(TripDetailUiEvent.NavigateMap(tripId))
            TripDetailIntent.OpenStats -> emitEvent(TripDetailUiEvent.NavigateStats(tripId))
            TripDetailIntent.OpenExport -> emitEvent(TripDetailUiEvent.NavigateExport(tripId))
            is TripDetailIntent.TravelerNameChanged ->
                updateState { copy(travelerNameDraft = intent.value, error = null) }
            TripDetailIntent.AddTraveler -> addLocalTraveler()
            is TripDetailIntent.InviteEmailChanged ->
                updateState { copy(inviteEmailDraft = intent.value, error = null) }
            TripDetailIntent.InviteTraveler -> sendInvite()
            TripDetailIntent.RefreshFx -> refreshFx()
            TripDetailIntent.Back -> emitEvent(TripDetailUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshTrip(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { handleFailure(it) }
        }
    }

    private fun addLocalTraveler() {
        launch {
            updateState { copy(isWorking = true, error = null) }
            addTraveler(
                state.value.tripId,
                CreateTravelerDraft(name = state.value.travelerNameDraft),
            )
                .onSuccess {
                    updateState { copy(isWorking = false, travelerNameDraft = "") }
                }
                .onFailure { handleFailure(it, working = true) }
        }
    }

    private fun sendInvite() {
        launch {
            updateState { copy(isWorking = true, error = null) }
            inviteTraveler(state.value.tripId, state.value.inviteEmailDraft)
                .onSuccess { invite ->
                    updateState {
                        copy(isWorking = false, inviteEmailDraft = "", lastInvite = invite)
                    }
                }
                .onFailure { handleFailure(it, working = true) }
        }
    }

    private fun refreshFx() {
        launch {
            updateState { copy(isWorking = true, error = null) }
            refreshExchangeRate(state.value.tripId)
                .onSuccess { updateState { copy(isWorking = false) } }
                .onFailure { handleFailure(it, working = true) }
        }
    }

    private fun handleFailure(throwable: Throwable, working: Boolean = false) {
        val uiError = throwable.asAppError().toUiError()
        updateState {
            copy(
                isLoading = false,
                isWorking = if (working) false else isWorking,
                error = uiError,
            )
        }
        if (uiError.action is UiErrorAction.Logout) emitEvent(TripDetailUiEvent.Logout)
    }
}
