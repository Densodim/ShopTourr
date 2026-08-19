package com.example.shoptourr.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripDayGroup
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.presentation.trip.TripDetailIntent
import com.example.shoptourr.presentation.trip.TripDetailUiEvent
import com.example.shoptourr.presentation.trip.TripDetailUiState
import com.example.shoptourr.presentation.trip.TripDetailViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.FullScreenLoading
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageQuickAction
import com.example.shoptourr.ui.components.VoyageQuickActions
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSectionHead
import com.example.shoptourr.ui.components.VoyageStat
import com.example.shoptourr.ui.components.VoyageStatStrip
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.i18n.tPlural
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.util.TripDayLabel
import com.example.shoptourr.ui.util.emoji
import com.example.shoptourr.ui.util.formatIsoDay
import com.example.shoptourr.ui.util.formatFxRate
import com.example.shoptourr.ui.util.formatted
import com.example.shoptourr.ui.util.labelKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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

    TripDetailContent(state = state, onIntent = viewModel::onIntent)
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

    val trip = detail.trip
    val currency = trip.budget.currency

    VoyageScreen {
        VoyageTopBar(
            title = trip.city,
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

        Spacer(Modifier.height(12.dp))
        TripHero(state)

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                label = t("spent"),
                value = money(detail.spentTotal, currency),
                progressPercent = detail.spendPercent(),
                emphasised = true,
                overBudget = detail.isOverBudget(),
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = t("remaining"),
                value = money(detail.remaining(), currency),
                caption = "${t("budget").lowercase()} ${money(trip.budget, currency)}",
                overBudget = detail.isOverBudget(),
            )
        }

        if (detail.purchases.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            VatSummary(
                vatTotal = money(detail.vatTotal(), currency),
                refundTotal = money(detail.taxRefundTotal(), currency),
                fxLine = trip.exchangeRate?.let { fx ->
                    "1 ${fx.tripCurrency} = ${formatFxRate(fx.rate)} ${fx.quoteCurrency}"
                },
                onRefreshFx = { onIntent(TripDetailIntent.RefreshFx) },
                isWorking = state.isWorking,
            )
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = it, onRetry = { onIntent(TripDetailIntent.Refresh) })
        }

        Spacer(Modifier.height(16.dp))
        VoyageQuickActions(
            modifier = Modifier.testTag(VoyageTestTags.TRIP_QUICK_ACTIONS),
            actions = listOf(
                VoyageQuickAction(label = t("stats"), onClick = { onIntent(TripDetailIntent.OpenStats) }),
                VoyageQuickAction(label = t("diary"), onClick = { onIntent(TripDetailIntent.OpenDiary) }),
                VoyageQuickAction(label = t("alerts"), onClick = { onIntent(TripDetailIntent.OpenAlerts) }),
                VoyageQuickAction(label = t("taxfree"), onClick = { onIntent(TripDetailIntent.OpenTaxFree) }),
            ),
        )

        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = t("new_purchase"),
            onClick = { onIntent(TripDetailIntent.AddPurchase) },
        )

        Spacer(Modifier.height(20.dp))
        if (detail.purchases.isEmpty()) {
            EmptyState(
                title = t("no_purchases"),
                message = t("no_purchases_sub"),
                actionLabel = t("add"),
                onAction = { onIntent(TripDetailIntent.AddPurchase) },
            )
        } else {
            CategoryChips(
                chips = state.categoryChips,
                selected = state.categoryFilter,
                totalCount = detail.purchases.size,
                onSelect = { onIntent(TripDetailIntent.CategoryFilterChanged(it)) },
                onClear = { state.categoryFilter?.let { onIntent(TripDetailIntent.CategoryFilterChanged(it)) } },
            )
            Spacer(Modifier.height(12.dp))
            val today = remember {
                kotlin.time.Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            state.visibleDays.forEach { day ->
                DayGroup(day = day, currency = currency, today = today.toString())
                Spacer(Modifier.height(16.dp))
            }
            if (state.hasMorePurchases) {
                VoyageButton(
                    text = t("load_more"),
                    onClick = { onIntent(TripDetailIntent.LoadMore) },
                    variant = VoyageButtonVariant.Secondary,
                    isLoading = state.isLoadingMore,
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        VoyageSection(title = t("travelers_section")) {
            if (trip.travelers.isEmpty()) {
                Text(t("only_you"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                trip.travelers.forEach { traveler ->
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
                label = t("traveler_name"),
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("add_traveler"),
                onClick = { onIntent(TripDetailIntent.AddTraveler) },
                isLoading = state.isWorking,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageTextField(
                value = state.inviteEmailDraft,
                onValueChange = { onIntent(TripDetailIntent.InviteEmailChanged(it)) },
                label = t("invite_email"),
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("invite_account"),
                onClick = { onIntent(TripDetailIntent.InviteTraveler) },
                isLoading = state.isWorking,
                variant = VoyageButtonVariant.Secondary,
            )
            state.lastInvite?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${it.email} · ${it.status.name.lowercase()}",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TripHero(state: TripDetailUiState) {
    val trip = state.detail?.trip ?: return
    VoyageSurfaceBlock {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = trip.flagEmoji ?: "✈",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            if (trip.status == TripStatus.ACTIVE) LiveChip()
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = trip.datesLabel ?: "${trip.startDate} — ${trip.endDate}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = trip.city,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(6.dp))
        val day = trip.currentDayNumber
        val total = trip.dayCount
        val dayLine = if (day != null && total != null) {
            t("day_n_of", "n" to day, "m" to total)
        } else {
            trip.country
        }
        Text(
            text = "$dayLine · ${tPlural("purchases", state.detail.purchases.size)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LiveChip() {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    progressPercent: Int? = null,
    emphasised: Boolean = false,
    overBudget: Boolean = false,
) {
    VoyageSurfaceBlock(modifier = modifier) {
        VoyageEyebrow(label)
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = when {
                overBudget -> MaterialTheme.colorScheme.error
                emphasised -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onBackground
            },
        )
        if (progressPercent != null) {
            Spacer(Modifier.height(8.dp))
            ProgressTrack(percent = progressPercent, overBudget = overBudget)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (caption != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressTrack(percent: Int, overBudget: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percent / 100f)
                .height(4.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(
                    if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                ),
        )
    }
}

@Composable
private fun VatSummary(
    vatTotal: String,
    refundTotal: String,
    fxLine: String?,
    onRefreshFx: () -> Unit,
    isWorking: Boolean,
) {
    // The block used to head itself "НДС" and then label its own first column
    // "НДС" again, with the FX rate crammed onto the heading row.
    Column(modifier = Modifier.fillMaxWidth()) {
        VoyageSectionHead(title = t("vat"), trailing = fxLine)
        Spacer(Modifier.height(16.dp))
        VoyageStatStrip(
            stats = listOf(
                VoyageStat(label = t("vat"), value = vatTotal),
                VoyageStat(label = t("tax_refund"), value = refundTotal, emphasised = true),
            ),
        )
        if (fxLine != null) {
            Spacer(Modifier.height(16.dp))
            VoyageButton(
                text = t("refresh_fx"),
                onClick = onRefreshFx,
                isLoading = isWorking,
                variant = VoyageButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun CategoryChips(
    chips: List<PurchaseCategory>,
    selected: PurchaseCategory?,
    totalCount: Int,
    onSelect: (PurchaseCategory) -> Unit,
    onClear: () -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Chip(
                label = "${t("all")} · $totalCount",
                active = selected == null,
                onClick = onClear,
            )
        }
        items(chips.size) { index ->
            val category = chips[index]
            Chip(
                label = "${category.emoji()}  ${t(category.labelKey())}",
                active = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}

@Composable
private fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    val border = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
            )
            .border(1.dp, border, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun DayGroup(day: TripDayGroup, currency: String, today: String) {
    val labelKey = TripDayLabel.keyFor(day.date, kotlinx.datetime.LocalDate.parse(today))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoyageEyebrow(
            text = labelKey?.let { t(it) } ?: formatIsoDay(day.date),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = money(day.total, currency),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
    Spacer(Modifier.height(8.dp))
    day.items.forEach { purchase ->
        PurchaseRow(purchase = purchase, currency = currency)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PurchaseRow(purchase: Purchase, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = purchase.category.emoji(),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = purchase.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (purchase.taxRefundEligible) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = t("taxfree"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            val sub = listOfNotNull(
                purchase.place?.takeIf { it.isNotBlank() },
                purchase.purchaseTime?.take(5),
                if (purchase.pendingSync) "sync…" else null,
            ).joinToString(" · ")
            if (sub.isNotBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = money(purchase.amount, currency),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** [currency] is the trip currency the caller has already reconciled against. */
private fun money(value: Money, @Suppress("UNUSED_PARAMETER") currency: String): String =
    value.formatted()
