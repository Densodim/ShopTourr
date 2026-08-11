package com.example.shoptourr.ui.taxfree

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
import com.example.shoptourr.presentation.taxfree.TaxFreeIntent
import com.example.shoptourr.presentation.taxfree.TaxFreeUiEvent
import com.example.shoptourr.presentation.taxfree.TaxFreeViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(TaxFreeIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Tax Free",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(TaxFreeIntent.Refresh) },
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
        if (state.isLoading && state.summary == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return
        }
        val summary = state.summary
        if (summary == null) {
            Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        Text(
            text = summary.rules.regionLabel,
            color = MaterialTheme.colorScheme.primary,
        )
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
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(summary.items, key = { it.purchaseId }) { item ->
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
