package de.example.todoalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.example.todoalarm.alarm.AlarmScheduler
import de.example.todoalarm.data.Task
import de.example.todoalarm.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val repository: TaskRepository,
    private val scheduler: AlarmScheduler,
) : ViewModel() {

    val pending: StateFlow<List<Task>> = repository.pending
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completed: StateFlow<List<Task>> = repository.completed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(task: Task) {
        viewModelScope.launch {
            scheduler.cancel(task.id)
            repository.delete(task)
        }
    }

    fun acknowledge(task: Task) {
        viewModelScope.launch {
            scheduler.cancel(task.id)
            repository.markCompleted(task.id)
        }
    }
}
