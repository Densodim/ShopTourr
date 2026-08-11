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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
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

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
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
    )
}

@Composable
internal fun LoginContent(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
) {
    VoyageScreen {
        VoyageEyebrow("Voyage")
        Spacer(Modifier.height(12.dp))
        Text(
            text = "VOYAGE",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Каждая поездка",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "— глава истории",
            style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = if (state.isRegisterMode) "Создать аккаунт" else "Войти",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        if (state.isRegisterMode) {
            VoyageTextField(
                value = state.displayName,
                onValueChange = { onIntent(AuthIntent.DisplayNameChanged(it)) },
                label = "Имя",
            )
            Spacer(Modifier.height(12.dp))
        }
        VoyageTextField(
            value = state.email,
            onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
            label = "Email",
        )
        Spacer(Modifier.height(12.dp))
        VoyageTextField(
            value = state.password,
            onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
            label = "Пароль",
            isPassword = true,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = if (state.isRegisterMode) "Зарегистрироваться" else "Войти",
            onClick = { onIntent(AuthIntent.Submit) },
            isLoading = state.isLoading,
        )
        Spacer(Modifier.height(8.dp))
        VoyageButton(
            text = if (state.isRegisterMode) "Уже есть аккаунт? Войти" else "Создать аккаунт",
            onClick = { onIntent(AuthIntent.ToggleMode) },
            variant = VoyageButtonVariant.Ghost,
            enabled = !state.isLoading,
        )
    }
}
