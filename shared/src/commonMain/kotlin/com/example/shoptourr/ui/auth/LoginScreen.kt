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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.presentation.auth.AuthIntent
import com.example.shoptourr.presentation.auth.AuthUiEvent
import com.example.shoptourr.presentation.auth.AuthUiState
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is AuthUiEvent.NavigateHome) onLoggedIn()
        }
    }

    LoginContent(
        state = state,
        onIntent = viewModel::onIntent,
        onForgotPassword = onForgotPassword,
        onBack = onBack,
    )
}

@Composable
internal fun LoginContent(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
    onForgotPassword: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    VoyageScreen {
        if (onBack != null) {
            // Arriving from Welcome with no way back left the only exit as the
            // system gesture, which quits the app from the auth stack's root.
            VoyageTopBar(onBack = onBack)
            Spacer(Modifier.height(14.dp))
        }
        VoyageEyebrow("Voyage")
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (state.isRegisterMode) t("hi_there") else t("welcome_back"),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (state.isRegisterMode) t("take_seconds") else t("where_left"),
            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // The page title already says which mode this is ("С возвращением" /
        // "Привет"); a second heading under it just repeated itself.
        Spacer(Modifier.height(34.dp))
        if (state.isRegisterMode) {
            VoyageTextField(
                value = state.displayName,
                onValueChange = { onIntent(AuthIntent.DisplayNameChanged(it)) },
                label = t("name"),
                testTag = VoyageTestTags.LOGIN_NAME,
                errorMessage = state.fieldErrors.displayName?.let { t(it) },
            )
            Spacer(Modifier.height(12.dp))
        }
        VoyageTextField(
            value = state.email,
            onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
            label = t("email"),
            testTag = VoyageTestTags.LOGIN_EMAIL,
            errorMessage = state.fieldErrors.email?.let { t(it) },
        )
        Spacer(Modifier.height(12.dp))
        VoyageTextField(
            value = state.password,
            onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
            label = t("password"),
            isPassword = true,
            testTag = VoyageTestTags.LOGIN_PASSWORD,
            errorMessage = state.fieldErrors.password?.let { t(it) },
        )
        if (!state.isRegisterMode) {
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("forgot_password"),
                onClick = onForgotPassword,
                variant = VoyageButtonVariant.Ghost,
                enabled = !state.isLoading,
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = if (state.isRegisterMode) t("sign_up") else t("sign_in"),
            onClick = { onIntent(AuthIntent.Submit) },
            isLoading = state.isLoading,
            modifier = Modifier.testTag(VoyageTestTags.LOGIN_SUBMIT),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = t("or_continue_with"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        VoyageButton(
            text = t("sign_in_google"),
            onClick = { onIntent(AuthIntent.SocialSignIn(SocialProvider.GOOGLE)) },
            variant = VoyageButtonVariant.Secondary,
            enabled = !state.isLoading,
            modifier = Modifier.testTag(VoyageTestTags.LOGIN_GOOGLE),
        )
        Spacer(Modifier.height(8.dp))
        VoyageButton(
            text = t("sign_in_apple"),
            onClick = { onIntent(AuthIntent.SocialSignIn(SocialProvider.APPLE)) },
            variant = VoyageButtonVariant.Secondary,
            enabled = !state.isLoading,
            modifier = Modifier.testTag(VoyageTestTags.LOGIN_APPLE),
        )
        Spacer(Modifier.height(8.dp))
        VoyageButton(
            text = if (state.isRegisterMode) {
                "${t("already_account")} ${t("sign_in")}"
            } else {
                "${t("no_account")} ${t("sign_up")}"
            },
            onClick = { onIntent(AuthIntent.ToggleMode) },
            variant = VoyageButtonVariant.Ghost,
            enabled = !state.isLoading,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = t("legal"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
