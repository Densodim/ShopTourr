package com.example.shoptourr.presentation.trip

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.domain.validation.FieldRules
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.toUiError
import com.example.shoptourr.ui.util.DatePickerFormats
import kotlinx.coroutines.launch

data class NewTripFieldErrors(
    val city: String? = null,
    val country: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budget: String? = null,
) {
    val hasErrors: Boolean =
        city != null || country != null || startDate != null || endDate != null || budget != null
}

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
    val fieldErrors: NewTripFieldErrors = NewTripFieldErrors(),
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
            is NewTripIntent.CityChanged -> updateState {
                copy(city = intent.value, error = null, fieldErrors = fieldErrors.copy(city = null))
            }
            is NewTripIntent.CountryChanged -> updateState {
                copy(country = intent.value, error = null, fieldErrors = fieldErrors.copy(country = null))
            }
            is NewTripIntent.StartDateChanged -> updateState {
                copy(
                    startDate = intent.value,
                    error = null,
                    fieldErrors = fieldErrors.copy(startDate = null, endDate = null),
                )
            }
            is NewTripIntent.EndDateChanged -> updateState {
                copy(endDate = intent.value, error = null, fieldErrors = fieldErrors.copy(endDate = null))
            }
            is NewTripIntent.BudgetChanged -> updateState {
                copy(budgetAmount = intent.value, error = null, fieldErrors = fieldErrors.copy(budget = null))
            }
            is NewTripIntent.CurrencyChanged -> updateState {
                copy(budgetCurrency = intent.value, error = null, fieldErrors = fieldErrors.copy(budget = null))
            }
            is NewTripIntent.QuoteCurrencyChanged ->
                updateState { copy(quoteCurrency = intent.value.uppercase(), error = null) }
            is NewTripIntent.TravelerDraftChanged ->
                updateState { copy(travelerDraft = intent.value, error = null) }
            NewTripIntent.AddTraveler -> {
                val name = state.value.travelerDraft.trim()
                if (!FieldRules.isTravelerName(name)) return
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
        val current = state.value
        val fieldErrors = validateFields(current)
        if (fieldErrors.hasErrors) {
            updateState { copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        launch {
            updateState { copy(isLoading = true, error = null) }
            val startDate = DatePickerFormats.normalizeUserDate(current.startDate) ?: current.startDate
            val endDate = DatePickerFormats.normalizeUserDate(current.endDate) ?: current.endDate
            val draft = CreateTripDraft(
                city = current.city,
                country = current.country,
                startDate = startDate,
                endDate = endDate,
                budget = Money.parse(current.budgetAmount, current.budgetCurrency),
                quoteCurrency = current.quoteCurrency.ifBlank { null },
                travelers = current.travelers,
            )
            createTrip(draft)
                .onSuccess { trip ->
                    updateState { copy(isLoading = false, error = null, fieldErrors = NewTripFieldErrors()) }
                    emitEvent(NewTripUiEvent.Created(trip.id))
                }
                .onFailure { throwable ->
                    val appError = throwable.asAppError()
                    val fieldKey = (appError as? AppError.Validation)?.message
                    updateState {
                        copy(
                            isLoading = false,
                            error = if (fieldKey == null) appError.toUiError() else null,
                            fieldErrors = mapValidationField(fieldKey),
                        )
                    }
                }
        }
    }

    private fun mapValidationField(fieldKey: String?): NewTripFieldErrors = when (fieldKey) {
        "city" -> NewTripFieldErrors(city = "validation_city_invalid")
        "country" -> NewTripFieldErrors(country = "validation_country_invalid")
        "startDate" -> NewTripFieldErrors(startDate = "validation_date_invalid")
        "endDate" -> NewTripFieldErrors(endDate = "validation_date_invalid")
        "dates" -> NewTripFieldErrors(endDate = "validation_dates_order")
        "budget" -> NewTripFieldErrors(budget = "validation_amount_positive")
        else -> NewTripFieldErrors()
    }

    private fun validateFields(state: NewTripUiState): NewTripFieldErrors {
        val city = when {
            state.city.trim().isEmpty() -> "validation_city_required"
            !FieldRules.isPlaceName(state.city.trim()) -> "validation_city_invalid"
            else -> null
        }
        val country = when {
            state.country.trim().isEmpty() -> "validation_country_required"
            !FieldRules.isPlaceName(state.country.trim()) -> "validation_country_invalid"
            else -> null
        }
        val startIso = DatePickerFormats.normalizeUserDate(state.startDate)
        val endIso = DatePickerFormats.normalizeUserDate(state.endDate)
        val startDate = when {
            state.startDate.isBlank() -> "validation_start_date_required"
            startIso == null -> "validation_date_invalid"
            else -> null
        }
        val endDate = when {
            state.endDate.isBlank() -> "validation_end_date_required"
            endIso == null -> "validation_date_invalid"
            startIso != null && endIso < startIso -> "validation_dates_order"
            else -> null
        }
        val budget = when {
            state.budgetAmount.isBlank() -> "validation_amount_required"
            else -> {
                val parsed = runCatching { Money.parse(state.budgetAmount, state.budgetCurrency) }.getOrNull()
                when {
                    parsed == null -> "validation_amount_invalid"
                    parsed.minorUnits <= 0 -> "validation_amount_positive"
                    else -> null
                }
            }
        }
        return NewTripFieldErrors(
            city = city,
            country = country,
            startDate = startDate,
            endDate = endDate,
            budget = budget,
        )
    }
}
