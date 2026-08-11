package com.example.shoptourr.ui.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.presentation.purchase.AddPurchaseIntent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiEvent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiState
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@Composable
fun AddPurchaseScreen(
    viewModel: AddPurchaseViewModel,
    onCreated: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is AddPurchaseUiEvent.Created) onCreated()
        }
    }

    AddPurchaseContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

@Composable
internal fun AddPurchaseContent(
    state: AddPurchaseUiState,
    onIntent: (AddPurchaseIntent) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val attachReceipt: suspend (String, ByteArray) -> Unit = { contentType, bytes ->
        onIntent(AddPurchaseIntent.AttachReceipt(contentType = contentType, bytes = bytes))
    }
    val galleryPicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
    ) { file ->
        if (file == null) return@rememberFilePickerLauncher
        scope.launch {
            attachReceipt(contentTypeForFileName(file.name), file.readBytes())
        }
    }
    val cameraPicker = rememberCameraPickerLauncher { file ->
        if (file == null) return@rememberCameraPickerLauncher
        scope.launch {
            attachReceipt(contentTypeForFileName(file.name), file.readBytes())
        }
    }

    VoyageScreen {
        VoyageTopBar(title = "Покупка", onBack = onBack)
        Spacer(Modifier.height(16.dp))
        VoyageSection(title = "Основное") {
            VoyageTextField(
                value = state.name,
                onValueChange = { onIntent(AddPurchaseIntent.NameChanged(it)) },
                label = "Название",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.amount,
                onValueChange = { onIntent(AddPurchaseIntent.AmountChanged(it)) },
                label = "Сумма",
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.currency,
                onValueChange = { onIntent(AddPurchaseIntent.CurrencyChanged(it)) },
                label = "Валюта",
            )
            Spacer(Modifier.height(12.dp))
            Text("Категория", color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurchaseCategory.entries.take(3).forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                        label = { Text(category.name.lowercase()) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurchaseCategory.entries.drop(3).forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                        label = { Text(category.name.lowercase()) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.place,
                onValueChange = { onIntent(AddPurchaseIntent.PlaceChanged(it)) },
                label = "Место",
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "VAT / Tax Free") {
            VoyageTextField(
                value = state.vatRatePercent,
                onValueChange = { onIntent(AddPurchaseIntent.VatRateChanged(it)) },
                label = "VAT %",
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.vatIncluded,
                    onCheckedChange = { onIntent(AddPurchaseIntent.VatIncludedChanged(it)) },
                )
                Text("VAT included", color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.taxRefundEligible,
                    onCheckedChange = { onIntent(AddPurchaseIntent.TaxRefundChanged(it)) },
                )
                Text("Tax Free", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = "Чек") {
            if (state.receiptMediaId != null) {
                Text(
                    text = if (state.isUploadingReceipt) "Загрузка чека…" else "Чек прикреплён",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VoyageButton(
                    text = "Галерея",
                    onClick = { galleryPicker.launch() },
                    enabled = !state.isUploadingReceipt && !state.isLoading,
                    isLoading = false,
                    variant = VoyageButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                )
                VoyageButton(
                    text = "Камера",
                    onClick = { cameraPicker.launch() },
                    enabled = !state.isUploadingReceipt && !state.isLoading,
                    isLoading = state.isUploadingReceipt,
                    variant = VoyageButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                )
            }
            state.ocr?.let { ocr ->
                Spacer(Modifier.height(12.dp))
                VoyageSurfaceBlock {
                    Text(
                        text = "OCR: ${ocr.suggestedName ?: "—"} · conf ${ocr.confidence}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    VoyageButton(
                        text = "Применить OCR",
                        onClick = { onIntent(AddPurchaseIntent.ApplyOcr) },
                        variant = VoyageButtonVariant.Ghost,
                    )
                }
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(24.dp))
        VoyageButton(
            text = "Сохранить",
            onClick = { onIntent(AddPurchaseIntent.Submit) },
            isLoading = state.isLoading,
        )
    }
}

internal fun contentTypeForFileName(name: String): String =
    when (name.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }
