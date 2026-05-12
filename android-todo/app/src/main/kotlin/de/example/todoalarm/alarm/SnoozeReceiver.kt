package de.example.todoalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import de.example.todoalarm.TodoApp
import de.example.todoalarm.notification.AlarmNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_SNOOZE) return
        val taskId = intent.getLongExtra(AlarmIntents.EXTRA_TASK_ID, -1L)
        val snoozeMs = intent.getLongExtra(AlarmIntents.EXTRA_SNOOZE_MS, -1L)
        if (taskId < 0 || snoozeMs <= 0) return

        val pendingResult = goAsync()
        val app = context.applicationContext as TodoApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationManagerCompat.from(context)
                    .cancel(AlarmNotifications.notificationId(taskId))
                app.scheduler.cancel(taskId)

                val task = app.repository.get(taskId) ?: return@launch
                if (task.isCompleted) return@launch

                val until = System.currentTimeMillis() + snoozeMs
                app.repository.snooze(taskId, until)
                app.scheduler.schedule(task.copy(snoozedUntil = until))
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to snooze task $taskId", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SnoozeReceiver"
    }
}
