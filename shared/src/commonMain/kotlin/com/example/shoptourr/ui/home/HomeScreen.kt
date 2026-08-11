package com.example.shoptourr.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.home.HomeIntent
import com.example.shoptourr.presentation.home.HomeUiState
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.FullScreenLoading
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.i18n.t

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateTrip: () -> Unit = {},
    onOpenTrip: (tripId: String) -> Unit = {},
    onAddPurchase: (tripId: String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenWishlist: () -> Unit = {},
    onOpenMap: (tripId: String) -> Unit = {},
    onOpenStats: (tripId: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onCreateTrip = onCreateTrip,
        onOpenTrip = onOpenTrip,
        onAddPurchase = onAddPurchase,
        onOpenProfile = onOpenProfile,
        onOpenWishlist = onOpenWishlist,
        onOpenMap = onOpenMap,
        onOpenStats = onOpenStats,
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onCreateTrip: () -> Unit,
    onOpenTrip: (tripId: String) -> Unit,
    onAddPurchase: (tripId: String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenMap: (tripId: String) -> Unit,
    onOpenStats: (tripId: String) -> Unit,
) {
    val snapshot = state.snapshot
    if (state.isLoading && snapshot == null) {
        FullScreenLoading()
        return
    }

    VoyageScreen {
        VoyageEyebrow("Voyage")
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (snapshot?.userName.isNullOrBlank()) {
                t("welcome_back")
            } else {
                "${t("hello")}, ${snapshot.userName}"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!state.isOnline) {
            Spacer(Modifier.height(12.dp))
            VoyageSurfaceBlock {
                Text(
                    text = "Офлайн",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Показываем кэш. Синхронизация продолжится при сети.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        VoyageSurfaceBlock {
            VoyageEyebrow(t("current_trip"))
            Spacer(Modifier.height(8.dp))
            Text(
                text = snapshot?.currentTripCity ?: "—",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${t("upcoming")} · ${snapshot?.upcomingCount ?: 0}   ${t("archive")} · ${snapshot?.archiveCount ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.error?.let { err ->
            Spacer(Modifier.height(16.dp))
            UiErrorBanner(
                error = err,
                onRetry = { onIntent(HomeIntent.Refresh) },
            )
        }

        if (snapshot?.currentTripId == null && !state.isLoading) {
            Spacer(Modifier.height(16.dp))
            EmptyState(
                title = t("new_trip"),
                message = t("where_to"),
                actionLabel = t("create_trip"),
                onAction = onCreateTrip,
            )
        }

        Spacer(Modifier.height(24.dp))
        VoyageSection(title = t("tab_home")) {
            VoyageButton(
                text = t("see_all"),
                onClick = { onIntent(HomeIntent.Refresh) },
                isLoading = state.isLoading,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageButton(text = t("new_trip"), onClick = onCreateTrip)
            Spacer(Modifier.height(10.dp))
            VoyageButton(
                text = t("profile"),
                onClick = onOpenProfile,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageButton(
                text = t("wishlist"),
                onClick = onOpenWishlist,
                variant = VoyageButtonVariant.Secondary,
            )
            val tripId = snapshot?.currentTripId
            if (tripId != null) {
                Spacer(Modifier.height(10.dp))
                VoyageButton(text = t("tab_trips"), onClick = { onOpenTrip(tripId) })
                Spacer(Modifier.height(10.dp))
                VoyageButton(
                    text = t("add"),
                    onClick = { onAddPurchase(tripId) },
                    variant = VoyageButtonVariant.Ghost,
                )
                Spacer(Modifier.height(10.dp))
                VoyageButton(
                    text = t("map"),
                    onClick = { onOpenMap(tripId) },
                    variant = VoyageButtonVariant.Secondary,
                )
                Spacer(Modifier.height(10.dp))
                VoyageButton(
                    text = t("stats"),
                    onClick = { onOpenStats(tripId) },
                    variant = VoyageButtonVariant.Secondary,
                )
            }
        }

        if (state.isLoading && snapshot != null) {
            LoadingBlock(label = "…")
        }
    }
}
