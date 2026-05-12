package de.example.todoalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.example.todoalarm.TodoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> Unit
            else -> return
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as TodoApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = app.repository.allPendingSnapshot()
                tasks.forEach { task -> app.scheduler.schedule(task) }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to reschedule alarms on ${intent.action}", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
