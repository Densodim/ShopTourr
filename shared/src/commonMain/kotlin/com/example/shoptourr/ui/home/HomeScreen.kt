package com.example.shoptourr.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.presentation.home.HomeIntent
import com.example.shoptourr.presentation.home.HomeUiState
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.FullScreenLoading
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageAvatar
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageProgressTrack
import com.example.shoptourr.ui.components.VoyageQuickAction
import com.example.shoptourr.ui.components.VoyageQuickActions
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSectionHead
import com.example.shoptourr.ui.components.VoyageStat
import com.example.shoptourr.ui.components.VoyageStatStrip
import com.example.shoptourr.ui.components.VoyageTripRow
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.i18n.tPlural
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.util.formatted
import com.example.shoptourr.ui.util.formattedRounded

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateTrip: () -> Unit = {},
    onOpenTrip: (tripId: String) -> Unit = {},
    onAddPurchase: (tripId: String) -> Unit = {},
    onOpenMap: (tripId: String) -> Unit = {},
    onOpenStats: (tripId: String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onCreateTrip = onCreateTrip,
        onOpenTrip = onOpenTrip,
        onAddPurchase = onAddPurchase,
        onOpenMap = onOpenMap,
        onOpenStats = onOpenStats,
        onOpenProfile = onOpenProfile,
    )
}

/**
 * Home answers three questions, in this order: how much is left on this trip,
 * what can I do about it right now, and where am I going next. Everything that
 * used to be a full-width button in a stack is now either the figure itself or
 * a row you can open.
 */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onCreateTrip: () -> Unit,
    onOpenTrip: (tripId: String) -> Unit,
    onAddPurchase: (tripId: String) -> Unit,
    onOpenMap: (tripId: String) -> Unit,
    onOpenStats: (tripId: String) -> Unit,
    onOpenProfile: () -> Unit = {},
) {
    val snapshot = state.snapshot
    if (state.isLoading && snapshot == null) {
        FullScreenLoading()
        return
    }

    val userName = snapshot?.userName.orEmpty()
    val currentTrip = snapshot?.currentTrip

    VoyageScreen {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                VoyageEyebrow(
                    text = t("hello"),
                    modifier = Modifier.testTag(VoyageTestTags.HOME_ROOT),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = userName.ifBlank { t("welcome_back") },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            VoyageAvatar(
                name = userName.ifBlank { "V" },
                onClick = onOpenProfile,
                contentDescription = t("profile"),
            )
        }

        // Status lines stay one-liners above the fold instead of the boxed panels
        // that used to push the trip itself off the screen.
        if (!state.isOnline) {
            Spacer(Modifier.height(16.dp))
            StatusLine(
                text = "${t("offline")} · ${t("offline_cache")}",
                modifier = Modifier.testTag(VoyageTestTags.HOME_OFFLINE_BANNER),
            )
        }
        if (state.conflictBanner) {
            Spacer(Modifier.height(12.dp))
            StatusLine(
                text = t("error_conflict_banner"),
                actionLabel = t("dismiss"),
                onAction = { onIntent(HomeIntent.DismissConflict) },
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(16.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(HomeIntent.Refresh) })
        }

        Spacer(Modifier.height(26.dp))

        if (currentTrip != null) {
            CurrentTripHero(
                trip = currentTrip,
                onOpenTrip = { onOpenTrip(currentTrip.id) },
                onAddPurchase = { onAddPurchase(currentTrip.id) },
                onOpenStats = { onOpenStats(currentTrip.id) },
                onOpenMap = { onOpenMap(currentTrip.id) },
            )
            Spacer(Modifier.height(24.dp))
            TripPaceStats(currentTrip)
            Spacer(Modifier.height(30.dp))
        } else if (!state.isLoading) {
            EmptyState(
                title = t("where_to"),
                message = t("welcome_sub"),
                actionLabel = t("create_trip"),
                onAction = onCreateTrip,
                actionTestTag = VoyageTestTags.HOME_NEW_TRIP,
            )
            Spacer(Modifier.height(12.dp))
        }

        val upcoming = snapshot?.upcoming.orEmpty()
        if (upcoming.isNotEmpty()) {
            VoyageSectionHead(title = t("upcoming"), trailing = upcoming.size.toString())
            upcoming.forEach { trip ->
                VoyageTripRow(
                    city = trip.city,
                    dates = trip.datesLabel ?: "${trip.startDate} — ${trip.endDate}",
                    flagEmoji = trip.flagEmoji,
                    amount = trip.budget.formatted(),
                    amountCaption = t("budget"),
                    onClick = { onOpenTrip(trip.id) },
                )
            }
            Spacer(Modifier.height(30.dp))
        }

        val archive = snapshot?.archive.orEmpty()
        if (archive.isNotEmpty()) {
            VoyageSectionHead(title = t("archive"), trailing = archive.size.toString())
            archive.forEach { trip ->
                VoyageTripRow(
                    city = trip.city,
                    dates = trip.datesLabel ?: "${trip.startDate} — ${trip.endDate}",
                    flagEmoji = trip.flagEmoji,
                    amount = trip.spent.formatted(),
                    amountCaption = tPlural("purchases", trip.purchaseCount),
                    muted = true,
                    onClick = { onOpenTrip(trip.id) },
                )
            }
            Spacer(Modifier.height(30.dp))
        }

        if (currentTrip != null || upcoming.isNotEmpty() || archive.isNotEmpty()) {
            VoyageButton(
                text = t("new_trip"),
                onClick = onCreateTrip,
                variant = VoyageButtonVariant.Secondary,
                modifier = Modifier.testTag(VoyageTestTags.HOME_NEW_TRIP),
            )
        }

        if (state.isLoading && snapshot != null) {
            LoadingBlock(label = t("loading"))
        }
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * The one figure the trip is about — what has been spent — with the budget it is
 * spending against and the three things worth doing from here.
 */
@Composable
private fun CurrentTripHero(
    trip: TripSummary,
    onOpenTrip: () -> Unit,
    onAddPurchase: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMap: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(VoyageTestTags.HOME_CURRENT_TRIP)
                .clickable(onClick = onOpenTrip),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dayLine = trip.currentDayNumber?.let { day ->
                trip.dayCount?.let { total -> t("day_n_of", "n" to day, "m" to total) }
            }
            Text(
                text = listOfNotNull(
                    trip.flagEmoji?.takeIf { it.isNotBlank() },
                    trip.city,
                    dayLine,
                ).joinToString(" · ").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${t("open").uppercase()}  →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(18.dp))
        VoyageEyebrow(t("spent"))
        Spacer(Modifier.height(10.dp))
        Text(
            text = trip.spent.formatted(),
            style = MaterialTheme.typography.displayLarge,
            color = if (trip.isOverBudget) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            maxLines = 1,
        )

        Spacer(Modifier.height(20.dp))
        VoyageProgressTrack(percent = trip.spendPercent(), overBudget = trip.isOverBudget)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${trip.spendPercent()}% · ${trip.budget.formatted()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            trip.dailyAllowance()?.let { perDay ->
                Text(
                    text = "${perDay.formattedRounded()} ${t("per_day")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        VoyageQuickActions(
            actions = listOf(
                VoyageQuickAction(
                    label = "+  ${t("add")}",
                    onClick = onAddPurchase,
                    testTag = VoyageTestTags.HOME_ADD_PURCHASE,
                ),
                VoyageQuickAction(label = t("stats"), onClick = onOpenStats),
                VoyageQuickAction(label = t("map"), onClick = onOpenMap),
            ),
            bordered = false,
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Remaining budget, days to go and the pace so far — the three pacing numbers. */
@Composable
private fun TripPaceStats(trip: TripSummary) {
    val stats = buildList {
        add(
            VoyageStat(
                label = t("remaining"),
                value = trip.remaining.formatted(),
                emphasised = trip.isOverBudget,
            ),
        )
        trip.daysLeft()?.let { days ->
            add(VoyageStat(label = t("days_left"), value = days.toString()))
        }
        trip.averagePerDay()?.let { average ->
            add(VoyageStat(label = t("daily_avg"), value = average.formattedRounded()))
        }
    }
    VoyageStatStrip(stats = stats)
}

@Composable
private fun StatusLine(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = actionLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}
