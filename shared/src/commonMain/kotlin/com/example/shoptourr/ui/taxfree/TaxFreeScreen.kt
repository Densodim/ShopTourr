package com.example.shoptourr.ui.taxfree

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
import com.example.shoptourr.presentation.taxfree.TaxFreeIntent
import com.example.shoptourr.presentation.taxfree.TaxFreeUiEvent
import com.example.shoptourr.presentation.taxfree.TaxFreeUiState
import com.example.shoptourr.presentation.taxfree.TaxFreeViewModel
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
fun TaxFreeScreen(
    viewModel: TaxFreeViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TaxFreeUiEvent.NavigateBack -> onBack()
                TaxFreeUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    TaxFreeContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun TaxFreeContent(
    state: TaxFreeUiState,
    onIntent: (TaxFreeIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Tax Free", onBack = { onIntent(TaxFreeIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = "Обновить",
            onClick = { onIntent(TaxFreeIntent.Refresh) },
            variant = VoyageButtonVariant.Secondary,
            isLoading = state.isLoading && state.summary != null,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(TaxFreeIntent.Refresh) })
        }
        Spacer(Modifier.height(16.dp))
        when {
            state.isLoading && state.summary == null -> LoadingBlock(label = "Считаем возврат…")
            state.summary == null -> EmptyState(
                title = "Нет данных",
                message = "Отметьте покупки как Tax Free, чтобы увидеть оценку",
                actionLabel = "Обновить",
                onAction = { onIntent(TaxFreeIntent.Refresh) },
            )
            else -> {
                val summary = state.summary!!
                VoyageSurfaceBlock {
                    Text(
                        text = summary.rules.regionLabel,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Мин. сумма: ${summary.rules.minimumPurchase.toDecimalString()} ${summary.rules.currency}",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Оценка возврата: ${summary.estimatedRefundTotal.toDecimalString()} ${summary.estimatedRefundTotal.currency}",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Подходящих покупок: ${summary.eligibleCount}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(20.dp))
                VoyageSection(title = "Покупки") {
                    summary.items.forEach { item ->
                        Text(item.name, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            text = "${item.amount.toDecimalString()} → ${item.estimatedRefund.toDecimalString()}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
