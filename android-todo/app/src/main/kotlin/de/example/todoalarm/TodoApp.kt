package de.example.todoalarm

import android.app.Application
import de.example.todoalarm.alarm.AlarmScheduler
import de.example.todoalarm.data.TaskDatabase
import de.example.todoalarm.data.TaskRepository
import de.example.todoalarm.notification.NotificationChannels

class TodoApp : Application() {

    val repository: TaskRepository by lazy {
        TaskRepository(TaskDatabase.get(this).taskDao())
    }

    val scheduler: AlarmScheduler by lazy { AlarmScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}
