package com.example.shoptourr.ui.trip

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.trip.NewTripIntent
import com.example.shoptourr.presentation.trip.NewTripUiEvent
import com.example.shoptourr.presentation.trip.NewTripUiState
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar

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
    VoyageScreen {
        VoyageTopBar(title = "Новая поездка", onBack = onBack)
        Spacer(Modifier.height(16.dp))
        VoyageSection(title = "Куда") {
            VoyageTextField(
                value = state.city,
                onValueChange = { onIntent(NewTripIntent.CityChanged(it)) },
                label = "Город",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.country,
                onValueChange = { onIntent(NewTripIntent.CountryChanged(it)) },
                label = "Страна",
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Даты") {
            VoyageTextField(
                value = state.startDate,
                onValueChange = { onIntent(NewTripIntent.StartDateChanged(it)) },
                label = "Начало (YYYY-MM-DD)",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.endDate,
                onValueChange = { onIntent(NewTripIntent.EndDateChanged(it)) },
                label = "Конец (YYYY-MM-DD)",
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Бюджет") {
            VoyageTextField(
                value = state.budgetAmount,
                onValueChange = { onIntent(NewTripIntent.BudgetChanged(it)) },
                label = "Бюджет",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.budgetCurrency,
                onValueChange = { onIntent(NewTripIntent.CurrencyChanged(it.uppercase())) },
                label = "Валюта",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.quoteCurrency,
                onValueChange = { onIntent(NewTripIntent.QuoteCurrencyChanged(it)) },
                label = "Quote FX (RUB…)",
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Участники") {
            VoyageTextField(
                value = state.travelerDraft,
                onValueChange = { onIntent(NewTripIntent.TravelerDraftChanged(it)) },
                label = "Участник",
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = "Добавить участника",
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
            text = "Создать",
            onClick = { onIntent(NewTripIntent.Submit) },
            isLoading = state.isLoading,
        )
    }
}
