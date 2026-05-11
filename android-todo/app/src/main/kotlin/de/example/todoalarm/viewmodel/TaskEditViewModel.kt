package de.example.todoalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.example.todoalarm.alarm.AlarmScheduler
import de.example.todoalarm.data.Task
import de.example.todoalarm.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskEditState(
    val id: Long = 0,
    val title: String = "",
    val notes: String = "",
    val dueAtMs: Long? = null,
    val dueActive: Boolean = false,
    val reAlertMinutes: Int = 3,
    val loading: Boolean = true,
)

class TaskEditViewModel(
    private val repository: TaskRepository,
    private val scheduler: AlarmScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskEditState(loading = false))
    val state: StateFlow<TaskEditState> = _state.asStateFlow()

    fun load(taskId: Long?) {
        if (taskId == null || taskId <= 0L) {
            _state.value = TaskEditState(loading = false)
            return
        }
        viewModelScope.launch {
            val task = repository.get(taskId) ?: return@launch
            _state.value = TaskEditState(
                id = task.id,
                title = task.title,
                notes = task.notes.orEmpty(),
                dueAtMs = task.dueAt,
                dueActive = task.dueAt != null,
                reAlertMinutes = (task.reAlertIntervalMs / 60_000L).toInt().coerceIn(1, 15),
                loading = false,
            )
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value) }
    fun updateNotes(value: String) { _state.value = _state.value.copy(notes = value) }
    fun updateDueActive(active: Boolean) { _state.value = _state.value.copy(dueActive = active) }
    fun updateDueAt(ms: Long?) { _state.value = _state.value.copy(dueAtMs = ms) }
    fun updateReAlertMinutes(value: Int) {
        _state.value = _state.value.copy(reAlertMinutes = value.coerceIn(1, 15))
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            val effectiveDue = if (s.dueActive) s.dueAtMs else null
            val reAlertMs = s.reAlertMinutes.coerceIn(1, 15) * 60_000L
            if (s.id == 0L) {
                val newTask = Task(
                    title = s.title.trim(),
                    notes = s.notes.takeIf { it.isNotBlank() },
                    dueAt = effectiveDue,
                    reAlertIntervalMs = reAlertMs,
                )
                val newId = repository.insert(newTask)
                if (effectiveDue != null) {
                    scheduler.schedule(newTask.copy(id = newId))
                }
            } else {
                val existing = repository.get(s.id) ?: return@launch
                val updated = existing.copy(
                    title = s.title.trim(),
                    notes = s.notes.takeIf { it.isNotBlank() },
                    dueAt = effectiveDue,
                    snoozedUntil = if (effectiveDue == null) null else existing.snoozedUntil,
                    reAlertIntervalMs = reAlertMs,
                )
                repository.update(updated)
                scheduler.cancel(updated.id)
                if (effectiveDue != null && !updated.isCompleted) {
                    scheduler.schedule(updated)
                }
            }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val s = _state.value
        if (s.id == 0L) {
            onDone()
            return
        }
        viewModelScope.launch {
            val existing = repository.get(s.id) ?: return@launch
            scheduler.cancel(existing.id)
            repository.delete(existing)
            onDone()
        }
    }
}
