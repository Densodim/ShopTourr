package com.example.shoptourr.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.example.shoptourr.presentation.diary.DiaryIntent
import com.example.shoptourr.presentation.diary.DiaryUiEvent
import com.example.shoptourr.presentation.diary.DiaryViewModel

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DiaryUiEvent.NavigateBack -> onBack()
                DiaryUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(DiaryIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Дневник",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.moodDraft,
            onValueChange = { viewModel.onIntent(DiaryIntent.MoodChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Настроение") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.textDraft,
            onValueChange = { viewModel.onIntent(DiaryIntent.TextChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Запись") },
            minLines = 3,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(DiaryIntent.Add) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            Text("Добавить")
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err.title, color = MaterialTheme.colorScheme.error)
            Text(err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.days.isEmpty()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                state.days.forEach { day ->
                    item(key = "day-${day.date}") {
                        Text(
                            text = day.date,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    items(day.entries, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.mood, color = MaterialTheme.colorScheme.onBackground)
                                Text(entry.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(
                                onClick = { viewModel.onIntent(DiaryIntent.Delete(entry.id)) },
                                enabled = !state.isSaving,
                            ) {
                                Text("Удалить")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
