package com.example.shoptourr.presentation.trip

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.launch

data class NewTripUiState(
    val city: String = "",
    val country: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val budgetAmount: String = "",
    val budgetCurrency: String = "EUR",
    val quoteCurrency: String = "RUB",
    val travelerDraft: String = "",
    val travelers: List<CreateTravelerDraft> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState

sealed interface NewTripIntent {
    data class CityChanged(val value: String) : NewTripIntent
    data class CountryChanged(val value: String) : NewTripIntent
    data class StartDateChanged(val value: String) : NewTripIntent
    data class EndDateChanged(val value: String) : NewTripIntent
    data class BudgetChanged(val value: String) : NewTripIntent
    data class CurrencyChanged(val value: String) : NewTripIntent
    data class QuoteCurrencyChanged(val value: String) : NewTripIntent
    data class TravelerDraftChanged(val value: String) : NewTripIntent
    data object AddTraveler : NewTripIntent
    data object Submit : NewTripIntent
}

sealed interface NewTripUiEvent : UiEvent {
    data class Created(val tripId: String) : NewTripUiEvent
}

class NewTripViewModel(
    private val createTrip: CreateTripUseCase,
) : BaseViewModel<NewTripUiState, NewTripUiEvent>(NewTripUiState()) {

    fun onIntent(intent: NewTripIntent) {
        when (intent) {
            is NewTripIntent.CityChanged -> updateState { copy(city = intent.value, error = null) }
            is NewTripIntent.CountryChanged -> updateState { copy(country = intent.value, error = null) }
            is NewTripIntent.StartDateChanged -> updateState { copy(startDate = intent.value, error = null) }
            is NewTripIntent.EndDateChanged -> updateState { copy(endDate = intent.value, error = null) }
            is NewTripIntent.BudgetChanged -> updateState { copy(budgetAmount = intent.value, error = null) }
            is NewTripIntent.CurrencyChanged -> updateState { copy(budgetCurrency = intent.value, error = null) }
            is NewTripIntent.QuoteCurrencyChanged ->
                updateState { copy(quoteCurrency = intent.value.uppercase(), error = null) }
            is NewTripIntent.TravelerDraftChanged ->
                updateState { copy(travelerDraft = intent.value, error = null) }
            NewTripIntent.AddTraveler -> {
                val name = state.value.travelerDraft.trim()
                if (name.isEmpty()) return
                updateState {
                    copy(
                        travelers = travelers + CreateTravelerDraft(name = name),
                        travelerDraft = "",
                    )
                }
            }
            NewTripIntent.Submit -> submit()
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            val current = state.value
            val draft = CreateTripDraft(
                city = current.city,
                country = current.country,
                startDate = current.startDate,
                endDate = current.endDate,
                budget = Money.parse(current.budgetAmount.ifBlank { "0" }, current.budgetCurrency),
                quoteCurrency = current.quoteCurrency.ifBlank { null },
                travelers = current.travelers,
            )
            createTrip(draft)
                .onSuccess { trip ->
                    updateState { copy(isLoading = false, error = null) }
                    emitEvent(NewTripUiEvent.Created(trip.id))
                }
                .onFailure { throwable ->
                    updateState {
                        copy(isLoading = false, error = throwable.asAppError().toUiError())
                    }
                }
        }
    }
}
