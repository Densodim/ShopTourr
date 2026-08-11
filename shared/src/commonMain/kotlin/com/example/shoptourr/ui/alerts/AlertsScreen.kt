package com.example.shoptourr.ui.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.alerts.AlertsIntent
import com.example.shoptourr.presentation.alerts.AlertsUiEvent
import com.example.shoptourr.presentation.alerts.AlertsViewModel

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AlertsUiEvent.NavigateBack -> onBack()
                AlertsUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(AlertsIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Алерты",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(AlertsIntent.Refresh) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Обновить")
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err.title, color = MaterialTheme.colorScheme.error)
            Text(err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.alerts.isEmpty()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (state.alerts.isEmpty()) {
            Text("Алертов нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.alerts, key = { it.id }) { alert ->
                    Text(
                        text = "${alert.severity}: ${alert.titleKey}",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(alert.bodyKey, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
