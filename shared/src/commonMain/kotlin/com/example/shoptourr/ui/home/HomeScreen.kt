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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateTrip: () -> Unit = {},
    onOpenTrip: (tripId: String) -> Unit = {},
    onAddPurchase: (tripId: String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenWishlist: () -> Unit = {},
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
            text = if (snapshot?.userName.isNullOrBlank()) "С возвращением" else "Привет, ${snapshot.userName}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))

        VoyageSurfaceBlock {
            VoyageEyebrow("Сейчас в поездке")
            Spacer(Modifier.height(8.dp))
            Text(
                text = snapshot?.currentTripCity ?: "Нет активной поездки",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Предстоящие · ${snapshot?.upcomingCount ?: 0}   Архив · ${snapshot?.archiveCount ?: 0}",
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
                title = "Начните главу",
                message = "Создайте поездку, чтобы трекать покупки, VAT и маршрут",
                actionLabel = "Новая поездка",
                onAction = onCreateTrip,
            )
        }

        Spacer(Modifier.height(24.dp))
        VoyageSection(title = "Действия") {
            VoyageButton(
                text = "Обновить",
                onClick = { onIntent(HomeIntent.Refresh) },
                isLoading = state.isLoading,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageButton(text = "Новая поездка", onClick = onCreateTrip)
            Spacer(Modifier.height(10.dp))
            VoyageButton(
                text = "Профиль",
                onClick = onOpenProfile,
                variant = VoyageButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
            VoyageButton(
                text = "Wishlist",
                onClick = onOpenWishlist,
                variant = VoyageButtonVariant.Secondary,
            )
            val tripId = snapshot?.currentTripId
            if (tripId != null) {
                Spacer(Modifier.height(10.dp))
                VoyageButton(text = "Открыть поездку", onClick = { onOpenTrip(tripId) })
                Spacer(Modifier.height(10.dp))
                VoyageButton(
                    text = "Добавить покупку",
                    onClick = { onAddPurchase(tripId) },
                    variant = VoyageButtonVariant.Ghost,
                )
            }
        }

        if (state.isLoading && snapshot != null) {
            LoadingBlock(label = "Обновляем…")
        }
    }
}
