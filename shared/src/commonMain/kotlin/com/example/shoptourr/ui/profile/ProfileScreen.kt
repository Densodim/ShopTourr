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
import com.example.shoptourr.ui.i18n.t

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
        VoyageTopBar(title = t("profile"), onBack = { onIntent(ProfileIntent.Back) })
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.profile?.email ?: "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        state.profile?.stats?.let { stats ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${t("trips_count")}: ${stats.tripsCount} · ${t("countries")}: ${stats.countriesCount} · ${t("wishlist")}: ${stats.wishlistCount}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.profile == null) {
            LoadingBlock(label = "…")
            return@VoyageScreen
        }
        VoyageSection(title = t("profile")) {
            VoyageTextField(
                value = state.displayNameDraft,
                onValueChange = { onIntent(ProfileIntent.DisplayNameChanged(it)) },
                label = t("name"),
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("edit_profile"),
                onClick = { onIntent(ProfileIntent.SaveProfile) },
                isLoading = state.isSaving,
            )
        }
        Spacer(Modifier.height(24.dp))
        VoyageSection(title = t("settings")) {
            Text(t("language"), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ru" to "RU", "en" to "EN").forEach { (tag, label) ->
                    FilterChip(
                        selected = state.localeDraft.trim().lowercase().startsWith(tag),
                        onClick = { onIntent(ProfileIntent.LocaleChanged(tag)) },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.currencyDraft,
                onValueChange = { onIntent(ProfileIntent.CurrencyChanged(it)) },
                label = t("currency_pref"),
            )
            Spacer(Modifier.height(12.dp))
            Text(t("theme"), color = MaterialTheme.colorScheme.onBackground)
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
                Text(t("push_notif"), color = MaterialTheme.colorScheme.onBackground)
            }
            VoyageButton(
                text = t("preferences"),
                onClick = { onIntent(ProfileIntent.SavePreferences) },
                isLoading = state.isSaving,
                variant = VoyageButtonVariant.Secondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        VoyageSurfaceBlock {
            Text(
                text = "${t("premium")}: ${state.profile?.premiumPlan?.name ?: "FREE"}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            if (state.profile?.isPremium != true) {
                Spacer(Modifier.height(12.dp))
                VoyageButton(
                    text = t("premium"),
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
            text = t("logout"),
            onClick = { onIntent(ProfileIntent.Logout) },
            enabled = !state.isSaving,
            variant = VoyageButtonVariant.Ghost,
        )
    }
}
