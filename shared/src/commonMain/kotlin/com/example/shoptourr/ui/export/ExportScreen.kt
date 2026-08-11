package com.example.shoptourr.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.shoptourr.presentation.export.ExportUiState
import com.example.shoptourr.presentation.export.ExportViewModel
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar

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

    ExportContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun ExportContent(
    state: ExportUiState,
    onIntent: (ExportIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = "Экспорт", onBack = { onIntent(ExportIntent.Back) })
        Spacer(Modifier.height(16.dp))
        VoyageSection(title = "Формат") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.format == ExportFormat.PDF,
                    onClick = { onIntent(ExportIntent.FormatChanged(ExportFormat.PDF)) },
                    label = { Text("PDF") },
                )
                FilterChip(
                    selected = state.format == ExportFormat.CSV,
                    onClick = { onIntent(ExportIntent.FormatChanged(ExportFormat.CSV)) },
                    label = { Text("CSV") },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.includeTaxFree,
                    onCheckedChange = { onIntent(ExportIntent.IncludeTaxFreeChanged(it)) },
                )
                Text("Tax Free", color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.includeDiary,
                    onCheckedChange = { onIntent(ExportIntent.IncludeDiaryChanged(it)) },
                )
                Text("Дневник", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(Modifier.height(16.dp))
        VoyageButton(
            text = "Создать экспорт",
            onClick = { onIntent(ExportIntent.Create) },
            enabled = !state.isLoading && !state.isPolling,
            isLoading = state.isLoading,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        if (state.isPolling) {
            LoadingBlock(label = "Готовим файл…")
        }
        state.job?.let { job ->
            Spacer(Modifier.height(16.dp))
            VoyageSurfaceBlock {
                Text(
                    text = "Статус: ${job.status.name.lowercase()}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                )
                when (job.status) {
                    ExportJobStatus.READY -> {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = job.downloadUrl ?: "Готово",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        job.expiresAt?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("Истекает: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    ExportJobStatus.FAILED -> {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = job.errorCode ?: "Ошибка экспорта",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    ExportJobStatus.EXPIRED -> {
                        Spacer(Modifier.height(8.dp))
                        Text("Ссылка истекла", color = MaterialTheme.colorScheme.error)
                    }
                    else -> Unit
                }
            }
        }
    }
}
