package com.example.shoptourr.ui.purchase

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
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.presentation.purchase.AddPurchaseIntent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiEvent
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = onBack) {
            Text("Назад")
        }
        Text(
            text = "Покупка",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onIntent(AddPurchaseIntent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.amount,
            onValueChange = { viewModel.onIntent(AddPurchaseIntent.AmountChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Сумма") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.currency,
            onValueChange = { viewModel.onIntent(AddPurchaseIntent.CurrencyChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Валюта") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Text("Категория", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PurchaseCategory.entries.take(3).forEach { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { viewModel.onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                    label = { Text(category.name.lowercase()) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PurchaseCategory.entries.drop(3).forEach { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { viewModel.onIntent(AddPurchaseIntent.CategoryChanged(category)) },
                    label = { Text(category.name.lowercase()) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.place,
            onValueChange = { viewModel.onIntent(AddPurchaseIntent.PlaceChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Место") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.vatRatePercent,
            onValueChange = { viewModel.onIntent(AddPurchaseIntent.VatRateChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("VAT %") },
            singleLine = true,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.vatIncluded,
                onCheckedChange = { viewModel.onIntent(AddPurchaseIntent.VatIncludedChanged(it)) },
            )
            Text("VAT included", color = MaterialTheme.colorScheme.onBackground)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.taxRefundEligible,
                onCheckedChange = { viewModel.onIntent(AddPurchaseIntent.TaxRefundChanged(it)) },
            )
            Text("Tax Free", color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.onIntent(
                    AddPurchaseIntent.AttachReceipt(
                        contentType = "image/jpeg",
                        bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()),
                    ),
                )
            },
            enabled = !state.isUploadingReceipt && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.receiptMediaId != null) "Чек прикреплён" else "Прикрепить чек (демо)")
        }
        if (state.isUploadingReceipt) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        state.ocr?.let { ocr ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "OCR: ${ocr.suggestedName ?: "—"} · conf ${ocr.confidence}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { viewModel.onIntent(AddPurchaseIntent.ApplyOcr) }) {
                Text("Применить OCR")
            }
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(text = err.title, color = MaterialTheme.colorScheme.error)
            Text(text = err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { viewModel.onIntent(AddPurchaseIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить")
            }
        }
    }
}
