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
import com.example.shoptourr.ui.components.VoyageCurrencyField
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel,
    onLoggedOut: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WishlistUiEvent.NavigateBack -> onBack?.invoke()
                WishlistUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    WishlistContent(
        state = state,
        onIntent = viewModel::onIntent,
        showBack = onBack != null,
    )
}

@Composable
internal fun WishlistContent(
    state: WishlistUiState,
    onIntent: (WishlistIntent) -> Unit,
    showBack: Boolean = true,
) {
    VoyageScreen {
        VoyageTopBar(
            title = t("wishlist"),
            onBack = if (showBack) {{ onIntent(WishlistIntent.Back) }} else null,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = t("wishlist_sub"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        VoyageSection(title = t("add")) {
            VoyageTextField(
                value = state.nameDraft,
                onValueChange = { onIntent(WishlistIntent.NameChanged(it)) },
                label = t("item_name"),
                errorMessage = state.fieldErrors.name?.let { t(it) },
            )
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.cityDraft,
                onValueChange = { onIntent(WishlistIntent.CityChanged(it)) },
                label = t("city"),
                errorMessage = state.fieldErrors.city?.let { t(it) },
            )
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.priceDraft,
                onValueChange = { onIntent(WishlistIntent.PriceChanged(it)) },
                label = t("amount"),
                errorMessage = state.fieldErrors.price?.let { t(it) },
            )
            Spacer(Modifier.height(8.dp))
            VoyageCurrencyField(
                value = state.currencyDraft,
                onValueChange = { onIntent(WishlistIntent.CurrencyChanged(it)) },
                label = t("currency_pref"),
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("add"),
                onClick = { onIntent(WishlistIntent.Add) },
                isLoading = state.isSaving,
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("see_all"),
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
            state.isLoading && state.items.isEmpty() -> LoadingBlock(label = "…")
            state.items.isEmpty() -> EmptyState(
                title = t("wishlist"),
                message = t("empty_wish"),
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
                            text = t("delete"),
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
