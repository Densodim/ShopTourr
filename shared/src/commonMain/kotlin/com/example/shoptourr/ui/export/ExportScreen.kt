package com.example.shoptourr.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
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
import com.example.shoptourr.ui.components.VoyageChip
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

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
        VoyageTopBar(title = t("export"), onBack = { onIntent(ExportIntent.Back) })
        Spacer(Modifier.height(16.dp))
        VoyageSection(title = t("format")) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.pdfEnabled) {
                    VoyageChip(
                        label = "PDF",
                        selected = state.format == ExportFormat.PDF,
                        onClick = { onIntent(ExportIntent.FormatChanged(ExportFormat.PDF)) },
                    )
                }
                VoyageChip(
                    label = "CSV",
                    selected = state.format == ExportFormat.CSV,
                    onClick = { onIntent(ExportIntent.FormatChanged(ExportFormat.CSV)) },
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
                Text(t("diary"), color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(Modifier.height(16.dp))
        VoyageButton(
            text = t("create_export"),
            onClick = { onIntent(ExportIntent.Create) },
            enabled = !state.isLoading && !state.isPolling,
            isLoading = state.isLoading,
        )
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        if (state.isPolling) {
            LoadingBlock(label = t("export_preparing"))
        }
        state.job?.let { job ->
            Spacer(Modifier.height(16.dp))
            VoyageSurfaceBlock {
                Text(
                    text = "${t("status")}: ${job.status.name.lowercase()}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                )
                when (job.status) {
                    ExportJobStatus.READY -> {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = job.downloadUrl ?: t("done"),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        job.expiresAt?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("${t("expires")}: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    ExportJobStatus.FAILED -> {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = job.errorCode ?: t("export_failed"),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    ExportJobStatus.EXPIRED -> {
                        Spacer(Modifier.height(8.dp))
                        Text(t("link_expired"), color = MaterialTheme.colorScheme.error)
                    }
                    else -> Unit
                }
            }
        }
    }
}
