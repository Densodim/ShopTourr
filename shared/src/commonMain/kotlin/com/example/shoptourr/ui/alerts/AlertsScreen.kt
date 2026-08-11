package com.example.shoptourr.ui.alerts

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.alerts.AlertsIntent
import com.example.shoptourr.presentation.alerts.AlertsUiEvent
import com.example.shoptourr.presentation.alerts.AlertsUiState
import com.example.shoptourr.presentation.alerts.AlertsViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageTopBar

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

    AlertsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun AlertsContent(
    state: AlertsUiState,
    onIntent: (AlertsIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Алерты", onBack = { onIntent(AlertsIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = "Обновить",
            onClick = { onIntent(AlertsIntent.Refresh) },
            variant = VoyageButtonVariant.Secondary,
            isLoading = state.isLoading && state.alerts.isNotEmpty(),
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(AlertsIntent.Refresh) })
        }
        Spacer(Modifier.height(16.dp))
        when {
            state.isLoading && state.alerts.isEmpty() -> LoadingBlock(label = "Загружаем…")
            state.alerts.isEmpty() -> EmptyState(
                title = "Алертов нет",
                message = "Покажем, когда бюджет или Tax Free потребуют внимания",
                actionLabel = "Обновить",
                onAction = { onIntent(AlertsIntent.Refresh) },
            )
            else -> {
                state.alerts.forEach { alert ->
                    Text(
                        text = "${alert.severity}: ${alert.titleKey}",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(alert.bodyKey, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
