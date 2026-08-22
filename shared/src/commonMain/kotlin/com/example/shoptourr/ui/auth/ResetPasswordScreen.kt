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
import com.example.shoptourr.presentation.auth.ResetPasswordIntent
import com.example.shoptourr.presentation.auth.ResetPasswordUiEvent
import com.example.shoptourr.presentation.auth.ResetPasswordUiState
import com.example.shoptourr.presentation.auth.ResetPasswordViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    prefillEmail: String = "",
    prefillToken: String = "",
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(prefillEmail, prefillToken) {
        if (prefillEmail.isNotBlank() || prefillToken.isNotBlank()) {
            viewModel.onIntent(ResetPasswordIntent.Prefill(prefillEmail, prefillToken))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ResetPasswordUiEvent.NavigateBack -> onBack()
                ResetPasswordUiEvent.NavigateToSignIn -> onSignIn()
            }
        }
    }

    ResetPasswordContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun ResetPasswordContent(
    state: ResetPasswordUiState,
    onIntent: (ResetPasswordIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = t("reset_password"), onBack = { onIntent(ResetPasswordIntent.Back) })
        Spacer(Modifier.height(12.dp))

        if (state.done) {
            VoyageSurfaceBlock {
                Text(
                    text = t("reset_done"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = t("reset_done_sub"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
            VoyageButton(
                text = t("sign_in"),
                onClick = { onIntent(ResetPasswordIntent.Finish) },
            )
            return@VoyageScreen
        }

        Text(
            text = t("reset_password_sub"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        VoyageTextField(
            value = state.email,
            onValueChange = { onIntent(ResetPasswordIntent.EmailChanged(it)) },
            label = t("email"),
            errorMessage = state.fieldErrors.email?.let { t(it) },
        )
        Spacer(Modifier.height(12.dp))
        VoyageTextField(
            value = state.token,
            onValueChange = { onIntent(ResetPasswordIntent.TokenChanged(it)) },
            label = t("reset_token"),
            errorMessage = state.fieldErrors.token?.let { t(it) },
        )
        Spacer(Modifier.height(12.dp))
        VoyageTextField(
            value = state.password,
            onValueChange = { onIntent(ResetPasswordIntent.PasswordChanged(it)) },
            label = t("password"),
            isPassword = true,
            errorMessage = state.fieldErrors.password?.let { t(it) },
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = t("save"),
            onClick = { onIntent(ResetPasswordIntent.Submit) },
            isLoading = state.isLoading,
        )
        Spacer(Modifier.height(8.dp))
        VoyageButton(
            text = t("back"),
            onClick = { onIntent(ResetPasswordIntent.Back) },
            variant = VoyageButtonVariant.Secondary,
        )
    }
}
