package de.example.todoalarm.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.content.getSystemService
import de.example.todoalarm.R

object NotificationChannels {

    const val ALARM = "alarm_fullscreen"

    fun ensureCreated(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return
        if (nm.getNotificationChannel(ALARM) != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val channel = NotificationChannel(
            ALARM,
            context.getString(R.string.channel_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_alarm_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 500, 500, 500, 500)
            setSound(sound, attrs)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
    }
}
