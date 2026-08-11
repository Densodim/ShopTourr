package com.example.shoptourr.ui.map

import androidx.compose.foundation.layout.Spacer
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
        VoyageTopBar(title = "Маршрут", onBack = { onIntent(RouteIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = "Обновить",
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
            state.isLoading && state.route == null -> LoadingBlock(label = "Строим маршрут…")
            state.route == null -> EmptyState(
                title = "Нет маршрута",
                message = "Точки появятся после покупок с геометками",
                actionLabel = "Обновить",
                onAction = { onIntent(RouteIntent.Refresh) },
            )
            else -> {
                val route = state.route!!
                VoyageSurfaceBlock {
                    Text(
                        text = "Точек: ${route.stopCount}",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    route.distanceMeters?.let {
                        Text(text = "Дистанция: ${it}m", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
                VoyageSection(title = "Остановки") {
                    route.stops.sortedBy { it.orderIndex }.forEach { stop ->
                        Text(
                            text = "${stop.orderIndex + 1}. ${stop.title}",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        stop.place?.let {
                            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        stop.point?.let {
                            Text("${it.lat}, ${it.lng}", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
