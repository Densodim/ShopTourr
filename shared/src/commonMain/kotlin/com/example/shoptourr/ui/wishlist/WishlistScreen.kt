package com.example.shoptourr.ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.wishlist.WishlistIntent
import com.example.shoptourr.presentation.wishlist.WishlistUiEvent
import com.example.shoptourr.presentation.wishlist.WishlistUiState
import com.example.shoptourr.presentation.wishlist.WishlistViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar

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

    WishlistContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun WishlistContent(
    state: WishlistUiState,
    onIntent: (WishlistIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Wishlist", onBack = { onIntent(WishlistIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageSection(title = "Добавить") {
            VoyageTextField(
                value = state.nameDraft,
                onValueChange = { onIntent(WishlistIntent.NameChanged(it)) },
                label = "Название",
            )
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.cityDraft,
                onValueChange = { onIntent(WishlistIntent.CityChanged(it)) },
                label = "Город",
            )
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.priceDraft,
                onValueChange = { onIntent(WishlistIntent.PriceChanged(it)) },
                label = "Цена",
            )
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.currencyDraft,
                onValueChange = { onIntent(WishlistIntent.CurrencyChanged(it)) },
                label = "Валюта",
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = "Добавить",
                onClick = { onIntent(WishlistIntent.Add) },
                isLoading = state.isSaving,
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = "Обновить",
                onClick = { onIntent(WishlistIntent.Refresh) },
                variant = VoyageButtonVariant.Secondary,
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(WishlistIntent.Refresh) })
        }
        Spacer(Modifier.height(20.dp))
        when {
            state.isLoading && state.items.isEmpty() -> LoadingBlock(label = "Загружаем…")
            state.items.isEmpty() -> EmptyState(
                title = "Список пуст",
                message = "Добавьте вещи, которые хотите найти в поездке",
            )
            else -> {
                state.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        VoyageButton(
                            text = "Удалить",
                            onClick = { onIntent(WishlistIntent.Delete(item.id)) },
                            enabled = !state.isSaving,
                            variant = VoyageButtonVariant.Ghost,
                            fillMaxWidth = false,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
