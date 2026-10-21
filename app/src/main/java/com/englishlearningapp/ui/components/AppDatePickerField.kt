package com.englishlearningapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    includeTime: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<String?>(null) }
    var displayValue by remember(value, includeTime) {
        mutableStateOf(value.toDisplayDate(includeTime))
    }
    val initialDateMillis = remember(value) {
        runCatching {
            LocalDate.parse(value.substringBefore(' '))
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    val currentTimeParts = value.substringAfter(' ', "00:00:00").split(":")
    val timePickerState = androidx.compose.material3.rememberTimePickerState(
        initialHour = currentTimeParts.getOrNull(0)?.toIntOrNull() ?: 0,
        initialMinute = currentTimeParts.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )

    OutlinedTextField(
        value = displayValue,
        onValueChange = { input ->
            displayValue = input
            input.toBackendDate(includeTime)?.let(onValueChange)
        },
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(if (includeTime) "dd/MM/yyyy HH:mm:ss" else "dd/MM/yyyy") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }, enabled = enabled) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select $label")
            }
        },
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                            if (includeTime) {
                                pendingDate = selectedDate
                                showTimePicker = true
                            } else {
                                onValueChange(selectedDate)
                            }
                        }
                        showPicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDate?.let { selectedDate ->
                            val selectedTime = String.format(
                                Locale.US,
                                "%02d:%02d:00",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onValueChange("$selectedDate $selectedTime")
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private val backendDateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT)
private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

private fun String.toDisplayDate(includeTime: Boolean): String {
    if (isBlank()) return ""
    return runCatching {
        val date = LocalDate.parse(substringBefore(' '), backendDateFormatter)
        val displayDate = date.format(displayDateFormatter)
        if (includeTime) "$displayDate ${substringAfter(' ', "00:00:00")}" else displayDate
    }.getOrDefault(this)
}

private fun String.toBackendDate(includeTime: Boolean): String? = runCatching {
    val datePart = substringBefore(' ').trim()
    val date = LocalDate.parse(datePart, displayDateFormatter)
    val backendDate = date.format(backendDateFormatter)
    if (!includeTime) return@runCatching backendDate

    val timePart = substringAfter(' ', "").trim()
    require(Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$").matches(timePart))
    "$backendDate $timePart"
}.getOrNull()
