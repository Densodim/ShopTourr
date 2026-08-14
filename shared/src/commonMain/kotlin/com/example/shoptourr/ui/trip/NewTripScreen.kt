package com.example.shoptourr.ui.trip

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.trip.NewTripIntent
import com.example.shoptourr.presentation.trip.NewTripUiEvent
import com.example.shoptourr.presentation.trip.NewTripUiState
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageDateField
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.util.DatePickerFormats

@Composable
fun NewTripScreen(
    viewModel: NewTripViewModel,
    onCreated: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is NewTripUiEvent.Created) onCreated()
        }
    }

    NewTripContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

@Composable
internal fun NewTripContent(
    state: NewTripUiState,
    onIntent: (NewTripIntent) -> Unit,
    onBack: () -> Unit,
) {
    val startDateMillis = remember(state.startDate) {
        state.startDate.takeIf { it.isNotBlank() }?.let(DatePickerFormats::isoToEpochMillis)
    }

    VoyageScreen {
        VoyageTopBar(title = t("create_trip"), onBack = onBack)
        Spacer(Modifier.height(16.dp))
        VoyageSection(title = t("where_to")) {
            VoyageTextField(
                value = state.city,
                onValueChange = { onIntent(NewTripIntent.CityChanged(it)) },
                label = t("city"),
                errorMessage = state.fieldErrors.city?.let { t(it) },
                testTag = VoyageTestTags.NEW_TRIP_CITY,
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.country,
                onValueChange = { onIntent(NewTripIntent.CountryChanged(it)) },
                label = t("country"),
                errorMessage = state.fieldErrors.country?.let { t(it) },
                testTag = VoyageTestTags.NEW_TRIP_COUNTRY,
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = t("dates_section")) {
            VoyageDateField(
                value = state.startDate,
                onValueChange = { onIntent(NewTripIntent.StartDateChanged(it)) },
                label = t("start_date"),
                errorMessage = state.fieldErrors.startDate?.let { t(it) },
                testTag = VoyageTestTags.NEW_TRIP_START,
            )
            Spacer(Modifier.height(12.dp))
            VoyageDateField(
                value = state.endDate,
                onValueChange = { onIntent(NewTripIntent.EndDateChanged(it)) },
                label = t("end_date"),
                errorMessage = state.fieldErrors.endDate?.let { t(it) },
                minDateMillis = startDateMillis,
                testTag = VoyageTestTags.NEW_TRIP_END,
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = t("budget")) {
            VoyageTextField(
                value = state.budgetAmount,
                onValueChange = { onIntent(NewTripIntent.BudgetChanged(it)) },
                label = t("budget"),
                errorMessage = state.fieldErrors.budget?.let { t(it) },
                testTag = VoyageTestTags.NEW_TRIP_BUDGET,
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.budgetCurrency,
                onValueChange = { onIntent(NewTripIntent.CurrencyChanged(it.uppercase())) },
                label = t("currency_pref"),
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.quoteCurrency,
                onValueChange = { onIntent(NewTripIntent.QuoteCurrencyChanged(it)) },
                label = t("quote_currency"),
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = t("travelers_section")) {
            VoyageTextField(
                value = state.travelerDraft,
                onValueChange = { onIntent(NewTripIntent.TravelerDraftChanged(it)) },
                label = t("traveler"),
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("add_traveler"),
                onClick = { onIntent(NewTripIntent.AddTraveler) },
                variant = VoyageButtonVariant.Ghost,
            )
            state.travelers.forEach { traveler ->
                Text(traveler.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = t("create_trip"),
            onClick = { onIntent(NewTripIntent.Submit) },
            isLoading = state.isLoading,
            modifier = Modifier.testTag(VoyageTestTags.NEW_TRIP_SUBMIT),
        )
    }
}
