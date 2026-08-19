package com.example.shoptourr.ui.legal

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.privacy.PrivacyIntent
import com.example.shoptourr.presentation.privacy.PrivacyUiEvent
import com.example.shoptourr.presentation.privacy.PrivacyViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun PrivacyScreen(
    viewModel: PrivacyViewModel,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PrivacyUiEvent.NavigateBack -> onBack()
                PrivacyUiEvent.AccountDeleted -> onAccountDeleted()
            }
        }
    }
    VoyageScreen {
        VoyageTopBar(title = t("privacy"), onBack = { viewModel.onIntent(PrivacyIntent.Back) })
        Spacer(Modifier.height(16.dp))
        VoyageSurfaceBlock {
            Text(
                text = t("privacy_body"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(16.dp))
        VoyageButton(
            text = if (state.analyticsEnabled) t("analytics_consent_on") else t("analytics_consent_off"),
            onClick = { viewModel.onIntent(PrivacyIntent.ToggleAnalyticsConsent) },
            variant = VoyageButtonVariant.Secondary,
        )
        Spacer(Modifier.height(20.dp))
        if (state.confirmDelete) {
            Text(
                text = t("delete_account_confirm"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("delete_account"),
                onClick = { viewModel.onIntent(PrivacyIntent.ConfirmDeleteAccount) },
                isLoading = state.isWorking,
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("cancel"),
                onClick = { viewModel.onIntent(PrivacyIntent.CancelDelete) },
                variant = VoyageButtonVariant.Secondary,
                enabled = !state.isWorking,
            )
        } else {
            VoyageButton(
                text = t("delete_account"),
                onClick = { viewModel.onIntent(PrivacyIntent.RequestDeleteAccount) },
                variant = VoyageButtonVariant.Secondary,
            )
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    LegalPage(
        title = t("about"),
        body = t("about_body"),
        onBack = onBack,
        footnote = "VOYAGE · v2.1.0",
    )
}

@Composable
fun SupportScreen(onBack: () -> Unit) {
    LegalPage(
        title = t("support"),
        body = t("support_body"),
        onBack = onBack,
    )
}

@Composable
private fun LegalPage(
    title: String,
    body: String,
    onBack: () -> Unit,
    footnote: String? = null,
) {
    VoyageScreen {
        VoyageTopBar(title = title, onBack = onBack)
        Spacer(Modifier.height(16.dp))
        VoyageSurfaceBlock {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (footnote != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = footnote,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
