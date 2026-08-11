package com.example.shoptourr.ui.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.home.HomeIntent
import com.example.shoptourr.presentation.home.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateTrip: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snapshot = state.snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = if (snapshot?.userName.isNullOrBlank()) "Voyage" else "Привет, ${snapshot.userName}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && snapshot == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Text(
                text = "Сейчас в поездке",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = snapshot?.currentTripCity ?: "—",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Предстоящие: ${snapshot?.upcomingCount ?: 0}",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Архив: ${snapshot?.archiveCount ?: 0}",
                color = MaterialTheme.colorScheme.onBackground,
            )
            state.error?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(text = err.title, color = MaterialTheme.colorScheme.error)
                Text(text = err.message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.onIntent(HomeIntent.Refresh) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Обновить")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onCreateTrip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Новая поездка")
            }
        }
    }
}
