package de.example.todoalarm.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.example.todoalarm.MainActivity
import de.example.todoalarm.R
import de.example.todoalarm.alarm.AcknowledgeReceiver
import de.example.todoalarm.alarm.AlarmIntents
import de.example.todoalarm.alarm.SnoozeReceiver
import de.example.todoalarm.data.Task
import de.example.todoalarm.ui.AlarmActivity
import de.example.todoalarm.util.SnoozeDurations

object AlarmNotifications {

    fun notificationId(taskId: Long): Int = (taskId and 0x7FFFFFFF).toInt()

    fun build(context: Context, task: Task): Notification {
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmIntents.EXTRA_TASK_ID, task.id)
        }
        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fsPending = PendingIntent.getActivity(
            context,
            AlarmIntents.fullscreenRequestCode(task.id),
            fullScreenIntent,
            piFlags,
        )

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            AlarmIntents.openRequestCode(task.id),
            contentIntent,
            piFlags,
        )

        val ackIntent = Intent(context, AcknowledgeReceiver::class.java).apply {
            action = AlarmIntents.ACTION_ACK
            putExtra(AlarmIntents.EXTRA_TASK_ID, task.id)
        }
        val ackPending = PendingIntent.getBroadcast(
            context,
            AlarmIntents.ackRequestCode(task.id),
            ackIntent,
            piFlags,
        )

        val builder = NotificationCompat.Builder(context, NotificationChannels.ALARM)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.alarm_title))
            .setContentText(task.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildString {
                        append(task.title)
                        if (!task.notes.isNullOrBlank()) {
                            append("\n\n")
                            append(task.notes)
                        }
                    })
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentPending)
            .setFullScreenIntent(fsPending, true)
            .addAction(
                R.drawable.ic_alarm,
                context.getString(R.string.alarm_acknowledge),
                ackPending,
            )

        SnoozeDurations.ALL_MS.forEachIndexed { index, durationMs ->
            val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
                action = AlarmIntents.ACTION_SNOOZE
                putExtra(AlarmIntents.EXTRA_TASK_ID, task.id)
                putExtra(AlarmIntents.EXTRA_SNOOZE_MS, durationMs)
            }
            val snoozePending = PendingIntent.getBroadcast(
                context,
                AlarmIntents.snoozeRequestCode(task.id, index),
                snoozeIntent,
                piFlags,
            )
            builder.addAction(
                R.drawable.ic_alarm,
                snoozeLabel(context, durationMs),
                snoozePending,
            )
        }

        return builder.build()
    }

    private fun snoozeLabel(context: Context, durationMs: Long): String = when (durationMs) {
        SnoozeDurations.ONE_MIN -> context.getString(R.string.snooze_1m)
        SnoozeDurations.THREE_MIN -> context.getString(R.string.snooze_3m)
        SnoozeDurations.TEN_MIN -> context.getString(R.string.snooze_10m)
        SnoozeDurations.THIRTY_MIN -> context.getString(R.string.snooze_30m)
        SnoozeDurations.ONE_HOUR -> context.getString(R.string.snooze_1h)
        SnoozeDurations.THREE_HOURS -> context.getString(R.string.snooze_3h)
        SnoozeDurations.ONE_DAY -> context.getString(R.string.snooze_1d)
        else -> "$durationMs ms"
    }
}
