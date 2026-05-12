package de.example.todoalarm.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.example.todoalarm.R
import de.example.todoalarm.data.Task
import de.example.todoalarm.util.TimeFormat
import de.example.todoalarm.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    vm: TaskListViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val pending by vm.pending.collectAsState()
    val completed by vm.completed.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.fab_add)) },
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader(stringResource(R.string.tab_open)) }
            if (pending.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.empty_open),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(pending, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onClick = { onEdit(task.id) },
                        onAcknowledge = { vm.acknowledge(task) },
                        onDelete = { vm.delete(task) },
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader(stringResource(R.string.tab_done)) }
            if (completed.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.empty_done),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(completed, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onClick = { onEdit(task.id) },
                        onAcknowledge = null,
                        onDelete = { vm.delete(task) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(Modifier.padding(top = 4.dp, bottom = 4.dp))
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onAcknowledge: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val due = task.effectiveTriggerAt ?: task.dueAt
                if (due != null) {
                    Text(
                        TimeFormat.format(due),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (!task.notes.isNullOrBlank()) {
                    Text(
                        task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
            }
            if (onAcknowledge != null) {
                IconButton(onClick = onAcknowledge) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.alarm_acknowledge))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}
