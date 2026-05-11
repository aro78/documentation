package de.example.todoalarm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.example.todoalarm.R
import de.example.todoalarm.viewmodel.TaskEditViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    vm: TaskEditViewModel,
    taskId: Long?,
    onDone: () -> Unit,
) {
    LaunchedEffect(taskId) { vm.load(taskId) }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == 0L) stringResource(R.string.edit_new_title)
                        else stringResource(R.string.edit_edit_title)
                    )
                },
                actions = {
                    TextButton(onClick = onDone) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    if (state.id != 0L) {
                        TextButton(onClick = { vm.delete(onDone) }) {
                            Text(stringResource(R.string.action_delete))
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
                DateTimeFields(
                    dueAtMs = state.dueAtMs,
                    onDueAtChange = vm::updateDueAt,
                )
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
}

@Composable
private fun DateTimeFields(
    dueAtMs: Long?,
    onDueAtChange: (Long?) -> Unit,
) {
    val initialMs = dueAtMs ?: defaultDueMs()
    var dateValue by remember(dueAtMs) { mutableStateOf(dateFormat.format(Date(initialMs))) }
    var timeValue by remember(dueAtMs) { mutableStateOf(timeFormat.format(Date(initialMs))) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = dateValue,
            onValueChange = {
                dateValue = it
                parseDateTime(it, timeValue)?.let(onDueAtChange)
            },
            label = { Text(stringResource(R.string.field_due_date)) },
            placeholder = { Text("TT.MM.JJJJ") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OutlinedTextField(
            value = timeValue,
            onValueChange = {
                timeValue = it
                parseDateTime(dateValue, it)?.let(onDueAtChange)
            },
            label = { Text(stringResource(R.string.field_due_time)) },
            placeholder = { Text("HH:MM") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

private fun defaultDueMs(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.HOUR_OF_DAY, 1)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun parseDateTime(date: String, time: String): Long? {
    return try {
        val datePart = dateFormat.parse(date) ?: return null
        val timePart = timeFormat.parse(time) ?: return null
        val cal = Calendar.getInstance()
        cal.time = datePart
        val tc = Calendar.getInstance().apply { this.time = timePart }
        cal.set(Calendar.HOUR_OF_DAY, tc.get(Calendar.HOUR_OF_DAY))
        cal.set(Calendar.MINUTE, tc.get(Calendar.MINUTE))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    } catch (_: Exception) {
        null
    }
}
