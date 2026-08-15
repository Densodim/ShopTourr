package com.example.shoptourr.ui.trip

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.trip.TripDetailIntent
import com.example.shoptourr.presentation.trip.TripDetailUiEvent
import com.example.shoptourr.presentation.trip.TripDetailUiState
import com.example.shoptourr.presentation.trip.TripDetailViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.FullScreenLoading
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageQuickAction
import com.example.shoptourr.ui.components.VoyageQuickActions
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags

@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    onAddPurchase: (tripId: String) -> Unit,
    onOpenDiary: (tripId: String) -> Unit = {},
    onOpenTaxFree: (tripId: String) -> Unit = {},
    onOpenAlerts: (tripId: String) -> Unit = {},
    onOpenMap: (tripId: String) -> Unit = {},
    onOpenStats: (tripId: String) -> Unit = {},
    onOpenExport: (tripId: String) -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TripDetailUiEvent.NavigateAddPurchase -> onAddPurchase(event.tripId)
                is TripDetailUiEvent.NavigateDiary -> onOpenDiary(event.tripId)
                is TripDetailUiEvent.NavigateTaxFree -> onOpenTaxFree(event.tripId)
                is TripDetailUiEvent.NavigateAlerts -> onOpenAlerts(event.tripId)
                is TripDetailUiEvent.NavigateMap -> onOpenMap(event.tripId)
                is TripDetailUiEvent.NavigateStats -> onOpenStats(event.tripId)
                is TripDetailUiEvent.NavigateExport -> onOpenExport(event.tripId)
                TripDetailUiEvent.NavigateBack -> onBack()
                TripDetailUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    TripDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun TripDetailContent(
    state: TripDetailUiState,
    onIntent: (TripDetailIntent) -> Unit,
) {
    if (state.isLoading && state.detail == null) {
        FullScreenLoading()
        return
    }

    val detail = state.detail
    if (detail == null) {
        VoyageScreen(scrollable = false) {
            VoyageTopBar(onBack = { onIntent(TripDetailIntent.Back) })
            Spacer(Modifier.height(16.dp))
            EmptyState(
                title = state.error?.let { t(it.titleKey) } ?: t("error_not_found_title"),
                message = state.error?.let { it.messageOverride ?: t(it.messageKey) }.orEmpty(),
            )
        }
        return
    }

    VoyageScreen {
        VoyageTopBar(
            title = detail.trip.city,
            onBack = { onIntent(TripDetailIntent.Back) },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onIntent(TripDetailIntent.OpenMap) }) {
                        Text(t("map"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onIntent(TripDetailIntent.OpenExport) }) {
                        Text(t("export"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
        )
        Text(
            text = detail.trip.country,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${detail.trip.startDate} — ${detail.trip.endDate}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        VoyageSurfaceBlock {
            Text(
                text = "Бюджет ${detail.trip.budget.toDecimalString()} ${detail.trip.budget.currency}",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Потрачено ${detail.spentTotal.toDecimalString()} ${detail.spentTotal.currency}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
            )
            detail.trip.exchangeRate?.let { fx ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "1 ${fx.tripCurrency} = ${fx.rate} ${fx.quoteCurrency}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                VoyageButton(
                    text = "Обновить курс",
                    onClick = { onIntent(TripDetailIntent.RefreshFx) },
                    isLoading = state.isWorking,
                    variant = VoyageButtonVariant.Secondary,
                )
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = it, onRetry = { onIntent(TripDetailIntent.Refresh) })
        }

        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Участники") {
            if (detail.trip.travelers.isEmpty()) {
                Text("Пока только вы", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                detail.trip.travelers.forEach { traveler ->
                    Text(
                        text = "${traveler.avatarGlyph}  ${traveler.name}" +
                            if (traveler.isOwner) " · owner" else "",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            VoyageTextField(
                value = state.travelerNameDraft,
                onValueChange = { onIntent(TripDetailIntent.TravelerNameChanged(it)) },
                label = "Имя участника",
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = "Добавить участника",
                onClick = { onIntent(TripDetailIntent.AddTraveler) },
                isLoading = state.isWorking,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageTextField(
                value = state.inviteEmailDraft,
                onValueChange = { onIntent(TripDetailIntent.InviteEmailChanged(it)) },
                label = "Email приглашения",
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = "Пригласить аккаунт",
                onClick = { onIntent(TripDetailIntent.InviteTraveler) },
                isLoading = state.isWorking,
            )
            state.lastInvite?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Invite ${it.status.name.lowercase()}: ${it.email}",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        VoyageQuickActions(
            modifier = Modifier.testTag(VoyageTestTags.TRIP_QUICK_ACTIONS),
            actions = listOf(
                VoyageQuickAction(label = t("stats"), onClick = { onIntent(TripDetailIntent.OpenStats) }),
                VoyageQuickAction(label = t("diary"), onClick = { onIntent(TripDetailIntent.OpenDiary) }),
                VoyageQuickAction(label = t("alerts"), onClick = { onIntent(TripDetailIntent.OpenAlerts) }),
                VoyageQuickAction(label = t("taxfree"), onClick = { onIntent(TripDetailIntent.OpenTaxFree) }),
            ),
        )
        Spacer(Modifier.height(16.dp))
        VoyageButton(
            text = t("new_purchase"),
            onClick = { onIntent(TripDetailIntent.AddPurchase) },
        )

        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Покупки · ${detail.purchases.size}") {
            if (detail.purchases.isEmpty()) {
                EmptyState(
                    title = "Покупок пока нет",
                    message = "Добавьте первую — с чеком и OCR, если нужно",
                    actionLabel = "Добавить покупку",
                    onAction = { onIntent(TripDetailIntent.AddPurchase) },
                )
            } else {
                detail.purchases.forEach { purchase ->
                    Text(purchase.name, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "${purchase.amount.toDecimalString()} ${purchase.amount.currency}" +
                            if (purchase.pendingSync) " · sync…" else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
