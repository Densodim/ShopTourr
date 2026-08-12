package com.example.shoptourr.ui.forceupdate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.ForceUpdateAction
import com.example.shoptourr.presentation.forceupdate.ForceUpdateIntent
import com.example.shoptourr.presentation.forceupdate.ForceUpdateViewModel
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.theme.VoyageTokens

@Composable
fun ForceUpdateGate(
    viewModel: ForceUpdateViewModel,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        when (state.action) {
            ForceUpdateAction.NONE -> Unit
            ForceUpdateAction.SOFT -> SoftUpdateBanner(
                onDismiss = { viewModel.onIntent(ForceUpdateIntent.DismissSoft) },
            )
            ForceUpdateAction.HARD -> HardUpdateBlocker()
        }
    }
}

@Composable
private fun SoftUpdateBanner(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        VoyageSurfaceBlock {
            Text(
                text = t("update_available"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = t("update_available_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("update_later"),
                onClick = onDismiss,
                variant = VoyageButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun HardUpdateBlocker() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoyageTokens.bg.copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = t("update_required"),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = t("update_required_sub"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
