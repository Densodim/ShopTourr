package com.example.shoptourr.ui.export

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.presentation.export.ExportIntent
import com.example.shoptourr.presentation.export.ExportUiEvent
import com.example.shoptourr.presentation.export.ExportViewModel

@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ExportUiEvent.NavigateBack -> onBack()
                ExportUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(ExportIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Экспорт",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        Text("Формат", color = MaterialTheme.colorScheme.onBackground)
        Row {
            TextButton(
                onClick = { viewModel.onIntent(ExportIntent.FormatChanged(ExportFormat.PDF)) },
            ) {
                Text(if (state.format == ExportFormat.PDF) "• PDF" else "PDF")
            }
            TextButton(
                onClick = { viewModel.onIntent(ExportIntent.FormatChanged(ExportFormat.CSV)) },
            ) {
                Text(if (state.format == ExportFormat.CSV) "• CSV" else "CSV")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.includeTaxFree,
                onCheckedChange = { viewModel.onIntent(ExportIntent.IncludeTaxFreeChanged(it)) },
            )
            Text("Tax Free", color = MaterialTheme.colorScheme.onBackground)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.includeDiary,
                onCheckedChange = { viewModel.onIntent(ExportIntent.IncludeDiaryChanged(it)) },
            )
            Text("Дневник", color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(ExportIntent.Create) },
            enabled = !state.isLoading && !state.isPolling,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Создать экспорт")
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err.title, color = MaterialTheme.colorScheme.error)
            Text(err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading || state.isPolling) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (state.isPolling) "Готовим файл…" else "Создаём…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.job?.let { job ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Статус: ${job.status.name.lowercase()}",
                color = MaterialTheme.colorScheme.onBackground,
            )
            when (job.status) {
                ExportJobStatus.READY -> {
                    Text(
                        text = job.downloadUrl ?: "Готово",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    job.expiresAt?.let {
                        Text("Истекает: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                ExportJobStatus.FAILED -> {
                    Text(
                        text = job.errorCode ?: "Ошибка экспорта",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                ExportJobStatus.EXPIRED -> {
                    Text("Ссылка истекла", color = MaterialTheme.colorScheme.error)
                }
                else -> Unit
            }
        }
    }
}
