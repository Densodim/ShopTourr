package com.example.shoptourr.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.presentation.lock.AppLockIntent
import com.example.shoptourr.presentation.lock.AppLockUiState
import com.example.shoptourr.presentation.lock.AppLockViewModel
import com.example.shoptourr.presentation.profile.ProfileIntent
import com.example.shoptourr.presentation.profile.ProfileUiEvent
import com.example.shoptourr.presentation.profile.ProfileUiState
import com.example.shoptourr.presentation.profile.ProfileViewModel
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageCurrencyField
import com.example.shoptourr.ui.components.VoyageChip
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageListRow
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags

@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    appLockViewModel: AppLockViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSupport: () -> Unit,
    onEditProfile: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val lockState by appLockViewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateBack -> onBack()
                ProfileUiEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    SettingsContent(
        state = state,
        lockState = lockState,
        onIntent = viewModel::onIntent,
        onLockIntent = appLockViewModel::onIntent,
        onOpenPrivacy = onOpenPrivacy,
        onOpenAbout = onOpenAbout,
        onOpenSupport = onOpenSupport,
        onEditProfile = onEditProfile,
    )
}

@Composable
internal fun SettingsContent(
    state: ProfileUiState,
    lockState: AppLockUiState,
    onIntent: (ProfileIntent) -> Unit,
    onLockIntent: (AppLockIntent) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSupport: () -> Unit,
    onEditProfile: () -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = t("settings"), onBack = { onIntent(ProfileIntent.Back) })
        if (state.isLoading && state.preferences == null) {
            Spacer(Modifier.height(24.dp))
            LoadingBlock(label = "…")
            return@VoyageScreen
        }

        Spacer(Modifier.height(20.dp))
        VoyageEyebrow(t("preferences"))
        Spacer(Modifier.height(8.dp))
        VoyageSurfaceBlock {
            Text(t("language"), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ru" to "RU", "en" to "EN").forEach { (tag, label) ->
                    VoyageChip(
                        label = label,
                        selected = state.localeDraft.trim().lowercase().startsWith(tag),
                        onClick = {
                            onIntent(ProfileIntent.LocaleChanged(tag))
                            onIntent(ProfileIntent.SavePreferences)
                        },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            VoyageCurrencyField(
                value = state.currencyDraft,
                onValueChange = {
                    onIntent(ProfileIntent.CurrencyChanged(it))
                    onIntent(ProfileIntent.SavePreferences)
                },
                label = t("currency_pref"),
                errorMessage = state.fieldErrors.currency?.let { t(it) },
            )
            Spacer(Modifier.height(12.dp))
            Text(t("theme"), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { theme ->
                    VoyageChip(
                        label = theme.name.lowercase(),
                        selected = state.themeDraft == theme,
                        onClick = {
                            onIntent(ProfileIntent.ThemeChanged(theme))
                            onIntent(ProfileIntent.SavePreferences)
                        },
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag(VoyageTestTags.SETTINGS_APP_LOCK),
            ) {
                Checkbox(
                    checked = lockState.enabled,
                    enabled = (lockState.available || lockState.enabled) && !lockState.authenticating,
                    onCheckedChange = { onLockIntent(AppLockIntent.SetEnabled(it)) },
                )
                Text(t("app_lock"), color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.pushDraft,
                    onCheckedChange = {
                        onIntent(ProfileIntent.PushChanged(it))
                        onIntent(ProfileIntent.SavePreferences)
                    },
                )
                Text(t("push_notif"), color = MaterialTheme.colorScheme.onBackground)
            }
            VoyageButton(
                text = t("save"),
                onClick = { onIntent(ProfileIntent.SavePreferences) },
                isLoading = state.isSaving,
                variant = VoyageButtonVariant.Secondary,
            )
        }

        Spacer(Modifier.height(24.dp))
        VoyageEyebrow(t("account"))
        Spacer(Modifier.height(8.dp))
        VoyageSurfaceBlock {
            VoyageListRow(title = t("edit_profile"), onClick = onEditProfile)
            HorizontalDivider()
            VoyageListRow(title = t("privacy"), onClick = onOpenPrivacy)
        }

        Spacer(Modifier.height(24.dp))
        VoyageEyebrow(t("about"))
        Spacer(Modifier.height(8.dp))
        VoyageSurfaceBlock {
            VoyageListRow(title = t("about"), detail = "v2.1.0", onClick = onOpenAbout)
            HorizontalDivider()
            VoyageListRow(title = t("support"), onClick = onOpenSupport)
        }

        (state.error ?: lockState.error)?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
    }
}
