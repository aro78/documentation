package de.example.todoalarm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.example.todoalarm.R
import de.example.todoalarm.util.TimeFormat
import de.example.todoalarm.viewmodel.TaskEditViewModel
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    vm: TaskEditViewModel,
    taskId: Long?,
    onDone: () -> Unit,
) {
    LaunchedEffect(taskId) { vm.load(taskId) }
    val state by vm.state.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == 0L) stringResource(R.string.edit_new_title)
                        else stringResource(R.string.edit_edit_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    if (state.id != 0L) {
                        IconButton(onClick = { vm.delete(onDone) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::updateTitle,
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::updateNotes,
                label = { Text(stringResource(R.string.field_notes)) },
                minLines = 2,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.field_due_active), modifier = Modifier.weight(1f))
                Switch(checked = state.dueActive, onCheckedChange = vm::updateDueActive)
            }

            if (state.dueActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = state.dueAtMs?.let { TimeFormat.format(it).substringBefore(",") }
                                ?: stringResource(R.string.field_due_date)
                        )
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = state.dueAtMs?.let { TimeFormat.format(it).substringAfter(", ", "") }
                                ?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.field_due_time)
                        )
                    }
                }
            }

            Column {
                Text("${stringResource(R.string.field_realert)}: ${state.reAlertMinutes}")
                Slider(
                    value = state.reAlertMinutes.toFloat(),
                    onValueChange = { vm.updateReAlertMinutes(it.toInt()) },
                    valueRange = 1f..15f,
                    steps = 13,
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.save(onDone) },
                enabled = state.title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueAtMs ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val datePart = pickerState.selectedDateMillis
                    if (datePart != null) {
                        val merged = mergeDate(datePart, state.dueAtMs)
                        vm.updateDueAt(merged)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val base = state.dueAtMs ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = base }
        val pickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TimePicker(state = pickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(onClick = {
                        val merged = mergeTime(state.dueAtMs ?: System.currentTimeMillis(),
                            pickerState.hour, pickerState.minute)
                        vm.updateDueAt(merged)
                        showTimePicker = false
                    }) { Text(stringResource(R.string.action_save)) }
                }
            }
        }
    }
}

private fun mergeDate(dateUtcMs: Long, existingMs: Long?): Long {
    // DatePicker returns midnight UTC of the picked date. We extract Y/M/D in UTC,
    // and combine with the existing hour/minute (or now's) in the local timezone.
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMs }
    val year = utc.get(Calendar.YEAR)
    val month = utc.get(Calendar.MONTH)
    val day = utc.get(Calendar.DAY_OF_MONTH)

    val base = existingMs ?: System.currentTimeMillis()
    val local = Calendar.getInstance().apply { timeInMillis = base }
    local.set(Calendar.YEAR, year)
    local.set(Calendar.MONTH, month)
    local.set(Calendar.DAY_OF_MONTH, day)
    return local.timeInMillis
}

private fun mergeTime(existingMs: Long, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = existingMs }
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
