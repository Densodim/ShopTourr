package com.example.shoptourr.ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.example.shoptourr.presentation.wishlist.WishlistIntent
import com.example.shoptourr.presentation.wishlist.WishlistUiEvent
import com.example.shoptourr.presentation.wishlist.WishlistViewModel

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WishlistUiEvent.NavigateBack -> onBack()
                WishlistUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = { viewModel.onIntent(WishlistIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Wishlist",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = state.nameDraft,
                onValueChange = { viewModel.onIntent(WishlistIntent.NameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название") },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.cityDraft,
                onValueChange = { viewModel.onIntent(WishlistIntent.CityChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Город") },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.priceDraft,
                onValueChange = { viewModel.onIntent(WishlistIntent.PriceChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Цена") },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.currencyDraft,
                onValueChange = { viewModel.onIntent(WishlistIntent.CurrencyChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Валюта") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.onIntent(WishlistIntent.Add) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                Text("Добавить")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onIntent(WishlistIntent.Refresh) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Обновить")
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(text = err.title, color = MaterialTheme.colorScheme.error)
            Text(text = err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.items.isEmpty()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.items, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "${item.city} · ${item.targetPrice.toDecimalString()} ${item.targetPrice.currency}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = { viewModel.onIntent(WishlistIntent.Delete(item.id)) },
                            enabled = !state.isSaving,
                        ) {
                            Text("Удалить")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
