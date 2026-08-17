package com.example.shoptourr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.util.DatePickerFormats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    testTag: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val tagged = if (testTag != null) {
        Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { contentDescription = label }
    } else {
        Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        VoyageFieldLabel(label)
        Spacer(Modifier.height(10.dp))
        VoyageUnderlineField(
            value = value,
            onValueChange = {},
            modifier = tagged.clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { showPicker = true },
            readOnly = true,
            isError = errorMessage != null,
            placeholder = { Text(t("date_pick_hint")) },
            trailingIcon = {
                TextButton(onClick = { showPicker = true }) {
                    Text(
                        text = t("date_pick_action").uppercase(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            },
            supportingText = errorMessage?.let { message ->
                { voyageFieldErrorText(message) }
            },
        )
    }

    if (showPicker) {
        val initialMillis = value.takeIf { it.isNotBlank() }?.let(DatePickerFormats::isoToEpochMillis)
        val selectableDates = remember(minDateMillis, maxDateMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (minDateMillis != null && utcTimeMillis < minDateMillis) return false
                    if (maxDateMillis != null && utcTimeMillis > maxDateMillis) return false
                    return true
                }

                override fun isSelectableYear(year: Int): Boolean = true
            }
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = selectableDates,
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onValueChange(DatePickerFormats.epochMillisToIso(millis))
                        }
                        showPicker = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) {
                    Text(t("date_pick_confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(t("date_pick_cancel"))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    headlineContentColor = MaterialTheme.colorScheme.onBackground,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dayContentColor = MaterialTheme.colorScheme.onBackground,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
