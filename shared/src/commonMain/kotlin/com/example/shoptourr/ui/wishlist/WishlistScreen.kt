package com.example.shoptourr.ui.wishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.WishlistItem
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
import com.example.shoptourr.ui.components.VoyageSectionHead
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.util.formatted

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

/**
 * The list is what the tab is for, so the list comes first. The four-field add
 * form used to open the screen, pushing every saved wish below the fold and
 * greeting a returning user with a wall of empty inputs.
 */
@Composable
internal fun WishlistContent(
    state: WishlistUiState,
    onIntent: (WishlistIntent) -> Unit,
    showBack: Boolean = true,
) {
    var isAdding by remember { mutableStateOf(false) }

    VoyageScreen {
        if (showBack) {
            VoyageTopBar(onBack = { onIntent(WishlistIntent.Back) })
            Spacer(Modifier.height(14.dp))
        }
        Text(
            text = t("wishlist"),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = t("wishlist_sub"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        state.error?.let { err ->
            Spacer(Modifier.height(16.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(WishlistIntent.Refresh) })
        }

        Spacer(Modifier.height(26.dp))
        VoyageSectionHead(
            title = t("all"),
            trailing = if (isAdding) t("dismiss") else "+  ${t("add")}",
            onTrailingClick = { isAdding = !isAdding },
        )

        if (isAdding) {
            Spacer(Modifier.height(18.dp))
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
            Spacer(Modifier.height(16.dp))
            VoyageButton(
                text = t("save"),
                onClick = { onIntent(WishlistIntent.Add) },
                isLoading = state.isSaving,
            )
            Spacer(Modifier.height(24.dp))
        }

        when {
            state.isLoading && state.items.isEmpty() -> LoadingBlock(label = t("loading"))
            state.items.isEmpty() -> EmptyState(
                title = t("empty_wish"),
                message = t("item_placeholder"),
                actionLabel = if (isAdding) null else t("add"),
                onAction = { isAdding = true },
            )
            else -> state.items.forEach { item ->
                WishRow(
                    item = item,
                    enabled = !state.isSaving,
                    canBuy = state.canBuy(item),
                    onBought = { onIntent(WishlistIntent.Bought(item.id)) },
                    onDelete = { onIntent(WishlistIntent.Delete(item.id)) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WishRow(
    item: WishlistItem,
    enabled: Boolean,
    canBuy: Boolean,
    onBought: () -> Unit,
    onDelete: () -> Unit,
) {
    val deleteLabel = t("delete")
    val boughtLabel = t("wish_bought")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.city.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.city.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (canBuy) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = boughtLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .testTag(VoyageTestTags.WISHLIST_BUY)
                            .semantics { contentDescription = boughtLabel }
                            .clickable(enabled = enabled, onClick = onBought),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.targetPrice.formatted(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                // A word-wide "Удалить" button on every row competed with the price
                // it sits next to; the cross carries the same action at label size.
                text = "✕",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .semantics { contentDescription = deleteLabel }
                    .clickable(enabled = enabled, onClick = onDelete)
                    .padding(6.dp),
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
