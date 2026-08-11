package com.example.shoptourr.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.trip.NewTripIntent
import com.example.shoptourr.presentation.trip.NewTripUiEvent
import com.example.shoptourr.presentation.trip.NewTripViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = onBack) {
            Text("Назад")
        }
        Text(
            text = "Новая поездка",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.city,
            onValueChange = { viewModel.onIntent(NewTripIntent.CityChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Город") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.country,
            onValueChange = { viewModel.onIntent(NewTripIntent.CountryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Страна") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.startDate,
            onValueChange = { viewModel.onIntent(NewTripIntent.StartDateChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Начало (YYYY-MM-DD)") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.endDate,
            onValueChange = { viewModel.onIntent(NewTripIntent.EndDateChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Конец (YYYY-MM-DD)") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.budgetAmount,
            onValueChange = { viewModel.onIntent(NewTripIntent.BudgetChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Бюджет") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.budgetCurrency,
            onValueChange = { viewModel.onIntent(NewTripIntent.CurrencyChanged(it.uppercase())) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Валюта") },
            singleLine = true,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(text = err.title, color = MaterialTheme.colorScheme.error)
            Text(text = err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { viewModel.onIntent(NewTripIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Создать")
            }
        }
    }
}
