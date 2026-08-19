package com.example.shoptourr.ui.stats

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.CategorySpend
import com.example.shoptourr.domain.model.DailySpend
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.presentation.stats.StatsIntent
import com.example.shoptourr.presentation.stats.StatsUiEvent
import com.example.shoptourr.presentation.stats.StatsUiState
import com.example.shoptourr.presentation.stats.StatsViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageProgressTrack
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSectionHead
import com.example.shoptourr.ui.components.VoyageStat
import com.example.shoptourr.ui.components.VoyageStatStrip
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.i18n.tPlural
import com.example.shoptourr.ui.util.emoji
import com.example.shoptourr.ui.util.formatIsoDay
import com.example.shoptourr.ui.util.formatted
import com.example.shoptourr.ui.util.formattedRounded
import com.example.shoptourr.ui.util.labelKey
import com.example.shoptourr.ui.util.sharePercent

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

/**
 * The figures were previously printed as `"souvenirs: 289.00 (0.3544)"` — an
 * untranslated enum and a raw fraction. Here each category is a named row with a
 * bar you can compare against its neighbours.
 */
@Composable
internal fun StatsContent(
    state: StatsUiState,
    onIntent: (StatsIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(
            title = t("stats"),
            onBack = { onIntent(StatsIntent.Back) },
            actions = {
                Text(
                    text = t("refresh").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onIntent(StatsIntent.Refresh) }
                        .padding(8.dp),
                )
            },
        )
        state.error?.let { err ->
            Spacer(Modifier.height(16.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(StatsIntent.Refresh) })
        }
        Spacer(Modifier.height(20.dp))
        when {
            state.isLoading && state.stats == null -> LoadingBlock(label = t("calculating"))
            state.stats == null -> EmptyState(
                title = t("no_data"),
                message = t("no_purchases_sub"),
                actionLabel = t("refresh"),
                onAction = { onIntent(StatsIntent.Refresh) },
            )
            else -> StatsBody(stats = state.stats!!)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatsBody(stats: TripStats) {
    val spendPercent = if (stats.budget.minorUnits <= 0L) {
        0
    } else {
        (stats.totalSpent.minorUnits * 100 / stats.budget.minorUnits).coerceIn(0L, 100L).toInt()
    }

    VoyageEyebrow(t("spent"))
    Spacer(Modifier.height(10.dp))
    Text(
        text = stats.totalSpent.formatted(),
        style = MaterialTheme.typography.displayLarge,
        color = if (stats.onBudget) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.error
        },
        maxLines = 1,
    )
    Spacer(Modifier.height(20.dp))
    VoyageProgressTrack(percent = spendPercent, overBudget = !stats.onBudget)
    Spacer(Modifier.height(10.dp))
    Text(
        text = "$spendPercent% · ${stats.budget.formatted()} · " +
            if (stats.onBudget) t("on_budget") else t("over_budget"),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.height(28.dp))
    VoyageStatStrip(
        stats = buildList {
            add(VoyageStat(label = t("remaining"), value = stats.remaining.formatted()))
            add(VoyageStat(label = t("daily_avg"), value = stats.dailyAverage.formattedRounded()))
            stats.topCategory?.let { top ->
                add(VoyageStat(label = t("top_category"), value = t(top.labelKey())))
            }
        },
    )

    if (stats.byCategory.isNotEmpty()) {
        Spacer(Modifier.height(34.dp))
        VoyageSectionHead(title = t("by_category"))
        stats.byCategory.forEach { item -> CategoryRow(item) }
    }

    if (stats.byDay.isNotEmpty()) {
        Spacer(Modifier.height(34.dp))
        VoyageSectionHead(title = t("by_day"))
        stats.byDay.forEach { day -> DayRow(day) }
    }
}

@Composable
private fun CategoryRow(item: CategorySpend) {
    val percent = sharePercent(item.share)
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.category.emoji()}  ${t(item.category.labelKey())}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.amount.formatted(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(9.dp))
        VoyageProgressTrack(percent = percent)
        Spacer(Modifier.height(7.dp))
        Text(
            text = "$percent% · ${tPlural("purchases", item.purchaseCount)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DayRow(day: DailySpend) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatIsoDay(day.date),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = tPlural("purchases", day.purchaseCount).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = day.amount.formatted(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}