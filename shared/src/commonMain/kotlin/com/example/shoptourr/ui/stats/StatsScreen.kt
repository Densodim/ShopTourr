package com.example.shoptourr.ui.stats

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
import com.example.shoptourr.presentation.stats.StatsIntent
import com.example.shoptourr.presentation.stats.StatsUiEvent
import com.example.shoptourr.presentation.stats.StatsUiState
import com.example.shoptourr.presentation.stats.StatsViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar

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

    StatsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun StatsContent(
    state: StatsUiState,
    onIntent: (StatsIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Статистика", onBack = { onIntent(StatsIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = "Обновить",
            onClick = { onIntent(StatsIntent.Refresh) },
            variant = VoyageButtonVariant.Secondary,
            isLoading = state.isLoading && state.stats != null,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(StatsIntent.Refresh) })
        }
        Spacer(Modifier.height(16.dp))
        when {
            state.isLoading && state.stats == null -> LoadingBlock(label = "Считаем…")
            state.stats == null -> EmptyState(
                title = "Нет статистики",
                message = "Появятся после покупок в поездке",
                actionLabel = "Обновить",
                onAction = { onIntent(StatsIntent.Refresh) },
            )
            else -> {
                val stats = state.stats!!
                VoyageSurfaceBlock {
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
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (stats.onBudget) "В бюджете" else "Сверх бюджета",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    stats.topCategory?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Топ категория: ${it.name.lowercase()}",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                VoyageSection(title = "По категориям") {
                    stats.byCategory.forEach { item ->
                        Text(
                            "${item.category.name.lowercase()}: ${item.amount.toDecimalString()} (${item.share})",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                VoyageSection(title = "По дням") {
                    stats.byDay.forEach { day ->
                        Text(
                            "${day.date}: ${day.amount.toDecimalString()} · ${day.purchaseCount}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
