package com.example.shoptourr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.SupportedCurrencies
import com.example.shoptourr.ui.i18n.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageCurrencyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    errorMessage: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = value.ifBlank { SupportedCurrencies.codes.first() }
    val options = remember(selected) {
        if (selected in SupportedCurrencies.codes) {
            SupportedCurrencies.codes
        } else {
            listOf(selected) + SupportedCurrencies.codes
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        VoyageFieldLabel(label)
        Spacer(Modifier.height(10.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            VoyageUnderlineField(
                value = currencyLabel(selected),
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                    .semantics { contentDescription = label },
                readOnly = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { message ->
                    { voyageFieldErrorText(message) }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(currencyLabel(code)) },
                        onClick = {
                            onValueChange(code)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun currencyLabel(code: String): String = "$code · ${t(SupportedCurrencies.nameKey(code))}"
