package com.example.shoptourr.ui.map

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.map.RouteIntent
import com.example.shoptourr.presentation.map.RouteUiEvent
import com.example.shoptourr.presentation.map.RouteUiState
import com.example.shoptourr.presentation.map.RouteViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun RouteScreen(
    viewModel: RouteViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RouteUiEvent.NavigateBack -> onBack()
                RouteUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    RouteContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun RouteContent(
    state: RouteUiState,
    onIntent: (RouteIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = t("map_title"), onBack = { onIntent(RouteIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = t("see_all"),
            onClick = { onIntent(RouteIntent.Refresh) },
            variant = VoyageButtonVariant.Secondary,
            isLoading = state.isLoading && state.route != null,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err, onRetry = { onIntent(RouteIntent.Refresh) })
        }
        Spacer(Modifier.height(16.dp))
        when {
            state.isLoading && state.route == null -> LoadingBlock(label = "…")
            state.route == null -> EmptyState(
                title = t("map_title"),
                message = t("stops"),
                actionLabel = t("see_all"),
                onAction = { onIntent(RouteIntent.Refresh) },
            )
            else -> {
                val route = checkNotNull(state.route)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VoyageSurfaceBlock(modifier = Modifier.weight(1f)) {
                        Text(
                            t("stops"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${route.stopCount}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    VoyageSurfaceBlock(modifier = Modifier.weight(1f)) {
                        Text(
                            t("distance"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = route.distanceMeters?.let { "${it}m" } ?: "—",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                val caption = route.stops.firstOrNull()?.point?.let { "${it.lat}° · ${it.lng}°" }
                RouteMapCanvas(stops = route.stops, caption = caption)
                Spacer(Modifier.height(20.dp))
                VoyageSection(title = t("stops")) {
                    route.stops.sortedBy { it.orderIndex }.forEach { stop ->
                        Text(
                            text = "${stop.orderIndex + 1}. ${stop.title}",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        stop.place?.let {
                            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        stop.amountSpentHere?.let {
                            Text(
                                "${it.toDecimalString()} ${it.currency}",
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
