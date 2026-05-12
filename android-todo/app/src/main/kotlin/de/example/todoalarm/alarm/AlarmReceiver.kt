package de.example.todoalarm.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.example.todoalarm.TodoApp
import de.example.todoalarm.notification.AlarmNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_FIRE) return
        val taskId = intent.getLongExtra(AlarmIntents.EXTRA_TASK_ID, -1L)
        if (taskId < 0) return

        val pendingResult = goAsync()
        val app = context.applicationContext as TodoApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.get(taskId) ?: return@launch
                if (task.isCompleted) return@launch

                val now = System.currentTimeMillis()
                app.repository.markFired(taskId, now)

                if (canPostNotifications(context)) {
                    val notification = AlarmNotifications.build(context, task)
                    NotificationManagerCompat.from(context)
                        .notify(AlarmNotifications.notificationId(taskId), notification)
                }

                // The persistence guarantee: schedule a re-alert. Only Acknowledge breaks the chain.
                app.scheduler.scheduleReAlert(taskId, task.reAlertIntervalMs, now)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to handle alarm for task $taskId", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
