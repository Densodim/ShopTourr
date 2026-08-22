package com.example.shoptourr.ui.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.profile.ProfileIntent
import com.example.shoptourr.presentation.profile.ProfileUiEvent
import com.example.shoptourr.presentation.profile.ProfileUiState
import com.example.shoptourr.presentation.profile.ProfileViewModel
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageListRow
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSectionHead
import com.example.shoptourr.ui.components.VoyageStat
import com.example.shoptourr.ui.components.VoyageStatStrip
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.util.formatIsoDay

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    editMode: Boolean = false,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateBack -> onBack?.invoke()
                ProfileUiEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    ProfileContent(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenSettings = onOpenSettings,
        onOpenSupport = onOpenSupport,
        onEditProfile = onEditProfile,
        onBack = onBack,
        editMode = editMode,
    )
}

@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupport: () -> Unit,
    onEditProfile: () -> Unit,
    onBack: (() -> Unit)?,
    editMode: Boolean,
) {
    VoyageScreen {
        VoyageTopBar(
            title = if (editMode) t("edit_profile") else t("profile"),
            onBack = onBack?.let { { onIntent(ProfileIntent.Back) } },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = state.profile?.displayName ?: "—",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = state.profile?.email ?: "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        state.profile?.memberSince?.let { since ->
            Spacer(Modifier.height(6.dp))
            Text(
                // The API sends a full ISO instant; printing it verbatim put
                // "2026-08-19T10:54:38.031291Z" under the user's own name.
                text = "${t("member_since")} ${formatIsoDay(since)}".uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        state.profile?.stats?.let { stats ->
            Spacer(Modifier.height(28.dp))
            VoyageStatStrip(
                stats = listOf(
                    VoyageStat(label = t("trips_count"), value = stats.tripsCount.toString()),
                    VoyageStat(label = t("countries"), value = stats.countriesCount.toString()),
                    VoyageStat(label = t("wishlist"), value = stats.wishlistCount.toString()),
                ),
            )
        }

        if (state.isLoading && state.profile == null) {
            Spacer(Modifier.height(24.dp))
            LoadingBlock(label = "…")
            return@VoyageScreen
        }

        if (editMode) {
            Spacer(Modifier.height(24.dp))
            VoyageEyebrow(t("edit_profile"))
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.displayNameDraft,
                onValueChange = { onIntent(ProfileIntent.DisplayNameChanged(it)) },
                label = t("name"),
                errorMessage = state.fieldErrors.displayName?.let { t(it) },
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("save"),
                onClick = { onIntent(ProfileIntent.SaveProfile) },
                isLoading = state.isSaving,
            )
        } else {
            Spacer(Modifier.height(32.dp))
            // Rows sit on bare paper and close on their own hairline; the boxed
            // panels around them doubled every rule and stacked card on card.
            VoyageSectionHead(title = t("account"))
            VoyageListRow(
                title = t("premium"),
                detail = state.profile?.premiumPlan?.name ?: "FREE",
                onClick = if (state.profile?.isPremium == true) {
                    null
                } else {
                    { onIntent(ProfileIntent.ActivatePlus) }
                },
            )
            VoyageListRow(title = t("edit_profile"), onClick = onEditProfile)
            VoyageListRow(title = t("settings"), onClick = onOpenSettings)
            VoyageListRow(title = t("support"), onClick = onOpenSupport)
            VoyageListRow(
                title = t("logout"),
                onClick = { onIntent(ProfileIntent.Logout) },
                destructive = true,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "VOYAGE · v2.1.0",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
    }
}
