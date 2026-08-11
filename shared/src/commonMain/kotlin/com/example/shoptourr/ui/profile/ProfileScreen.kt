package com.example.shoptourr.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.presentation.profile.ProfileIntent
import com.example.shoptourr.presentation.profile.ProfileUiEvent
import com.example.shoptourr.presentation.profile.ProfileViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = { viewModel.onIntent(ProfileIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.profile?.email ?: "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.profile?.stats?.let { stats ->
            Text(
                text = "Поездки: ${stats.tripsCount} · Страны: ${stats.countriesCount} · Wishlist: ${stats.wishlistCount}",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.profile == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return
        }
        OutlinedTextField(
            value = state.displayNameDraft,
            onValueChange = { viewModel.onIntent(ProfileIntent.DisplayNameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Имя") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(ProfileIntent.SaveProfile) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            Text("Сохранить профиль")
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.localeDraft,
            onValueChange = { viewModel.onIntent(ProfileIntent.LocaleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Locale") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.currencyDraft,
            onValueChange = { viewModel.onIntent(ProfileIntent.CurrencyChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Валюта") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Text("Тема", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { theme ->
                FilterChip(
                    selected = state.themeDraft == theme,
                    onClick = { viewModel.onIntent(ProfileIntent.ThemeChanged(theme)) },
                    label = { Text(theme.name.lowercase()) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.pushDraft,
                onCheckedChange = { viewModel.onIntent(ProfileIntent.PushChanged(it)) },
            )
            Text("Push-уведомления", color = MaterialTheme.colorScheme.onBackground)
        }
        Button(
            onClick = { viewModel.onIntent(ProfileIntent.SavePreferences) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            Text("Сохранить настройки")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Premium: ${state.profile?.premiumPlan?.name ?: "FREE"}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (state.profile?.isPremium != true) {
            Button(
                onClick = { viewModel.onIntent(ProfileIntent.ActivatePlus) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                Text("Активировать Plus")
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(text = err.title, color = MaterialTheme.colorScheme.error)
            Text(text = err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.onIntent(ProfileIntent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            Text("Выйти")
        }
    }
}
