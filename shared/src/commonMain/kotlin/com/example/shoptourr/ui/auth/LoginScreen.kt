package com.example.shoptourr.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.auth.AuthIntent
import com.example.shoptourr.presentation.auth.AuthUiEvent
import com.example.shoptourr.presentation.auth.AuthViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
        Spacer(Modifier.height(32.dp))
        if (state.isRegisterMode) {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { viewModel.onIntent(AuthIntent.DisplayNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Имя") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onIntent(AuthIntent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err.title, color = MaterialTheme.colorScheme.error)
            Text(err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { viewModel.onIntent(AuthIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isRegisterMode) "Зарегистрироваться" else "Войти")
            }
            TextButton(onClick = { viewModel.onIntent(AuthIntent.ToggleMode) }) {
                Text(
                    if (state.isRegisterMode) "Уже есть аккаунт? Войти" else "Создать аккаунт",
                )
            }
        }
    }
}
