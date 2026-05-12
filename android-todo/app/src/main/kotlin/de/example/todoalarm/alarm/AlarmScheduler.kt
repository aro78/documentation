package de.example.todoalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import de.example.todoalarm.MainActivity
import de.example.todoalarm.data.Task

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = checkNotNull(context.getSystemService())

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    /**
     * Schedule (or reschedule) the alarm for [task]. Uses [task.effectiveTriggerAt]; if null or in
     * the past relative to [now], we still schedule for `max(triggerAt, now)` so the receiver fires
     * immediately and the re-alert chain takes over.
     */
    fun schedule(task: Task, now: Long = System.currentTimeMillis()) {
        val trigger = task.effectiveTriggerAt ?: return
        val triggerAt = maxOf(trigger, now)
        val pi = firePendingIntent(task.id)
        val showIntent = openAppPendingIntent(task.id)
        val info = AlarmManager.AlarmClockInfo(triggerAt, showIntent)
        alarmManager.setAlarmClock(info, pi)
    }

    /** Schedule a re-alert in [intervalMs] from now. Same request code overwrites prior pending. */
    fun scheduleReAlert(taskId: Long, intervalMs: Long, now: Long = System.currentTimeMillis()) {
        val pi = firePendingIntent(taskId)
        val showIntent = openAppPendingIntent(taskId)
        val info = AlarmManager.AlarmClockInfo(now + intervalMs, showIntent)
        alarmManager.setAlarmClock(info, pi)
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(firePendingIntent(taskId))
    }

    private fun firePendingIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmIntents.ACTION_FIRE
            putExtra(AlarmIntents.EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            AlarmIntents.fireRequestCode(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openAppPendingIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            AlarmIntents.openRequestCode(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
