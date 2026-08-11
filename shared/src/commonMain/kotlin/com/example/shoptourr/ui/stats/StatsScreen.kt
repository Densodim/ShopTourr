package com.example.shoptourr.ui.stats

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
import com.example.shoptourr.presentation.stats.StatsIntent
import com.example.shoptourr.presentation.stats.StatsUiEvent
import com.example.shoptourr.presentation.stats.StatsViewModel

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                StatsUiEvent.NavigateBack -> onBack()
                StatsUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(StatsIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(StatsIntent.Refresh) },
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
        if (state.isLoading && state.stats == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return
        }
        val stats = state.stats
        if (stats == null) {
            Text("Нет статистики", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        Text(
            text = "Потрачено: ${stats.totalSpent.toDecimalString()} ${stats.totalSpent.currency}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Бюджет: ${stats.budget.toDecimalString()} ${stats.budget.currency}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Остаток: ${stats.remaining.toDecimalString()} ${stats.remaining.currency}",
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (stats.onBudget) "В бюджете" else "Сверх бюджета",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        stats.topCategory?.let {
            Text("Топ категория: ${it.name.lowercase()}", color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(12.dp))
        Text("По категориям", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(stats.byCategory, key = { it.category.name }) { item ->
                Text(
                    "${item.category.name.lowercase()}: ${item.amount.toDecimalString()} (${item.share})",
                    color = MaterialTheme.colorScheme.onBackground,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("По дням", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            items(stats.byDay, key = { it.date }) { day ->
                Text(
                    "${day.date}: ${day.amount.toDecimalString()} · ${day.purchaseCount}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
