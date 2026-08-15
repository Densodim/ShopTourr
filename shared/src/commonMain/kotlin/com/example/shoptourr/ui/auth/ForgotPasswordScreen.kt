package com.example.shoptourr.ui.auth

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
import com.example.shoptourr.presentation.auth.ForgotPasswordIntent
import com.example.shoptourr.presentation.auth.ForgotPasswordUiEvent
import com.example.shoptourr.presentation.auth.ForgotPasswordUiState
import com.example.shoptourr.presentation.auth.ForgotPasswordViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onBack: () -> Unit,
    onEnterCode: (email: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is ForgotPasswordUiEvent.NavigateBack) onBack()
        }
    }
    ForgotPasswordContent(
        state = state,
        onIntent = viewModel::onIntent,
        onEnterCode = { onEnterCode(state.email) },
    )
}

@Composable
internal fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onIntent: (ForgotPasswordIntent) -> Unit,
    onEnterCode: () -> Unit = {},
) {
    VoyageScreen {
        VoyageTopBar(title = t("forgot_password"), onBack = { onIntent(ForgotPasswordIntent.Back) })
        Spacer(Modifier.height(12.dp))
        Text(
            text = t("forgot_password"),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = t("forgot_password_sub"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        if (state.sent) {
            VoyageSurfaceBlock {
                Text(
                    text = t("reset_sent"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(16.dp))
            VoyageButton(
                text = t("enter_code"),
                onClick = onEnterCode,
            )
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("back"),
                onClick = { onIntent(ForgotPasswordIntent.Back) },
                variant = VoyageButtonVariant.Secondary,
            )
        } else {
            VoyageTextField(
                value = state.email,
                onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
                label = t("email"),
            )
            state.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                UiErrorBanner(error = err)
            }
            Spacer(Modifier.height(24.dp))
            VoyageButton(
                text = t("send_reset"),
                onClick = { onIntent(ForgotPasswordIntent.Submit) },
                isLoading = state.isLoading,
            )
        }
    }
}
