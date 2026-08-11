package com.example.shoptourr.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.presentation.profile.ProfileIntent
import com.example.shoptourr.presentation.profile.ProfileUiEvent
import com.example.shoptourr.presentation.profile.ProfileUiState
import com.example.shoptourr.presentation.profile.ProfileViewModel
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateBack -> onBack()
                ProfileUiEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    ProfileContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Профиль", onBack = { onIntent(ProfileIntent.Back) })
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.profile?.email ?: "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        state.profile?.stats?.let { stats ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Поездки: ${stats.tripsCount} · Страны: ${stats.countriesCount} · Wishlist: ${stats.wishlistCount}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.profile == null) {
            LoadingBlock(label = "Загружаем профиль…")
            return@VoyageScreen
        }
        VoyageSection(title = "Профиль") {
            VoyageTextField(
                value = state.displayNameDraft,
                onValueChange = { onIntent(ProfileIntent.DisplayNameChanged(it)) },
                label = "Имя",
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = "Сохранить профиль",
                onClick = { onIntent(ProfileIntent.SaveProfile) },
                isLoading = state.isSaving,
            )
        }
        Spacer(Modifier.height(24.dp))
        VoyageSection(title = "Настройки") {
            VoyageTextField(
                value = state.localeDraft,
                onValueChange = { onIntent(ProfileIntent.LocaleChanged(it)) },
                label = "Locale",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.currencyDraft,
                onValueChange = { onIntent(ProfileIntent.CurrencyChanged(it)) },
                label = "Валюта",
            )
            Spacer(Modifier.height(12.dp))
            Text("Тема", color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { theme ->
                    FilterChip(
                        selected = state.themeDraft == theme,
                        onClick = { onIntent(ProfileIntent.ThemeChanged(theme)) },
                        label = { Text(theme.name.lowercase()) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.pushDraft,
                    onCheckedChange = { onIntent(ProfileIntent.PushChanged(it)) },
                )
                Text("Push-уведомления", color = MaterialTheme.colorScheme.onBackground)
            }
            VoyageButton(
                text = "Сохранить настройки",
                onClick = { onIntent(ProfileIntent.SavePreferences) },
                isLoading = state.isSaving,
                variant = VoyageButtonVariant.Secondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        VoyageSurfaceBlock {
            Text(
                text = "Premium: ${state.profile?.premiumPlan?.name ?: "FREE"}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            if (state.profile?.isPremium != true) {
                Spacer(Modifier.height(12.dp))
                VoyageButton(
                    text = "Активировать Plus",
                    onClick = { onIntent(ProfileIntent.ActivatePlus) },
                    enabled = !state.isSaving,
                )
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = "Выйти",
            onClick = { onIntent(ProfileIntent.Logout) },
            enabled = !state.isSaving,
            variant = VoyageButtonVariant.Ghost,
        )
    }
}
