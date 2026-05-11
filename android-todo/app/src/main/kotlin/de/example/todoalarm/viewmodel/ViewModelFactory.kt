package de.example.todoalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.example.todoalarm.alarm.AlarmScheduler
import de.example.todoalarm.data.TaskRepository

class ViewModelFactory(
    private val repository: TaskRepository,
    private val scheduler: AlarmScheduler,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(TaskListViewModel::class.java) ->
            TaskListViewModel(repository, scheduler) as T
        modelClass.isAssignableFrom(TaskEditViewModel::class.java) ->
            TaskEditViewModel(repository, scheduler) as T
        else -> throw IllegalArgumentException("Unknown VM ${modelClass.name}")
    }
}
