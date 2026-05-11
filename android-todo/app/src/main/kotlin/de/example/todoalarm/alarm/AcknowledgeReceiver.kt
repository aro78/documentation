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

class AcknowledgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_ACK) return
        val taskId = intent.getLongExtra(AlarmIntents.EXTRA_TASK_ID, -1L)
        if (taskId < 0) return

        val pendingResult = goAsync()
        val app = context.applicationContext as TodoApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationManagerCompat.from(context)
                    .cancel(AlarmNotifications.notificationId(taskId))
                // This is the ONLY way the re-alert chain stops:
                app.scheduler.cancel(taskId)
                app.repository.markCompleted(taskId)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to acknowledge task $taskId", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AcknowledgeReceiver"
    }
}
