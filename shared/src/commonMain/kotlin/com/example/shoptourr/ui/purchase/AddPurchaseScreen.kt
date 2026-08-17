package com.example.shoptourr.ui.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.presentation.purchase.AddPurchaseIntent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiEvent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiState
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageCurrencyField
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags
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
        VoyageTopBar(title = t("new_purchase"), onBack = onBack)
        Spacer(Modifier.height(16.dp))
        // No section eyebrow here: the field below already carries this exact label.
        Column(modifier = Modifier.fillMaxWidth()) {
            VoyageTextField(
                value = state.name,
                onValueChange = { onIntent(AddPurchaseIntent.NameChanged(it)) },
                label = t("item_name"),
                testTag = VoyageTestTags.ADD_PURCHASE_NAME,
            )
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.amount,
                onValueChange = { onIntent(AddPurchaseIntent.AmountChanged(it)) },
                label = t("amount"),
                testTag = VoyageTestTags.ADD_PURCHASE_AMOUNT,
            )
            Spacer(Modifier.height(12.dp))
            VoyageCurrencyField(
                value = state.currency,
                onValueChange = { onIntent(AddPurchaseIntent.CurrencyChanged(it)) },
                label = t("currency_pref"),
            )
            Spacer(Modifier.height(12.dp))
            Text(t("category"), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurchaseCategory.entries.take(3).forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                        label = { Text(t(category.i18nKey())) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurchaseCategory.entries.drop(3).forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                        label = { Text(t(category.i18nKey())) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            VoyageTextField(
                value = state.place,
                onValueChange = { onIntent(AddPurchaseIntent.PlaceChanged(it)) },
                label = t("place"),
            )
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = t("vat")) {
            VoyageTextField(
                value = state.vatRatePercent,
                onValueChange = { onIntent(AddPurchaseIntent.VatRateChanged(it)) },
                label = t("vat"),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.vatIncluded,
                    onCheckedChange = { onIntent(AddPurchaseIntent.VatIncludedChanged(it)) },
                )
                Text(t("vat_included"), color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.taxRefundEligible,
                    onCheckedChange = { onIntent(AddPurchaseIntent.TaxRefundChanged(it)) },
                )
                Text(t("taxfree"), color = MaterialTheme.colorScheme.onBackground)
            }
        }
        if (state.travelers.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            VoyageSection(title = t("split_with")) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.travelers.forEach { traveler ->
                        FilterChip(
                            selected = traveler.id in state.selectedTravelerIds,
                            onClick = { onIntent(AddPurchaseIntent.ToggleTraveler(traveler.id)) },
                            label = {
                                Text("${traveler.avatarGlyph} ${traveler.name}")
                            },
                        )
                    }
                }
                state.yourShare?.let { share ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${t("your_share")}: ${share.toDecimalString()} ${share.currency}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        VoyageSection(title = t("receipt")) {
            if (state.receiptMediaId != null) {
                Text(
                    text = if (state.isUploadingReceipt) t("scanning") else t("detected"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VoyageButton(
                    text = t("from_gallery"),
                    onClick = { galleryPicker.launch() },
                    enabled = !state.isUploadingReceipt && !state.isLoading,
                    isLoading = false,
                    variant = VoyageButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                )
                VoyageButton(
                    text = t("from_camera"),
                    onClick = { cameraPicker.launch() },
                    enabled = !state.isUploadingReceipt && !state.isLoading,
                    isLoading = state.isUploadingReceipt,
                    variant = VoyageButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                )
            }
            state.ocr?.takeIf { state.ocrAssistEnabled }?.let { ocr ->
                Spacer(Modifier.height(12.dp))
                VoyageSurfaceBlock {
                    Text(
                        text = "${t("detected")}: ${ocr.suggestedName ?: "—"} · conf ${ocr.confidence}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    VoyageButton(
                        text = t("scan_receipt"),
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
            text = t("save"),
            onClick = { onIntent(AddPurchaseIntent.Submit) },
            isLoading = state.isLoading,
            modifier = Modifier.testTag(VoyageTestTags.ADD_PURCHASE_SUBMIT),
        )
    }
}

private fun PurchaseCategory.i18nKey(): String = when (this) {
    PurchaseCategory.FOOD -> "cat_food"
    PurchaseCategory.TRANSPORT -> "cat_transport"
    PurchaseCategory.SOUVENIRS -> "cat_souvenirs"
    PurchaseCategory.HOTEL -> "cat_hotel"
    PurchaseCategory.CULTURE -> "cat_culture"
    PurchaseCategory.OTHER -> "cat_other"
}

internal fun contentTypeForFileName(name: String): String =
    when (name.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }
