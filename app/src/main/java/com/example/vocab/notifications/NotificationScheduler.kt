// NotificationScheduler.kt
package com.example.vocab.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.vocab.model.NotificationSettings
import java.util.*

class NotificationScheduler {
    fun scheduleNotifications(context: Context, settings: NotificationSettings) {
        if (!settings.enabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel old alarms
        cancelAll(context)

        // Set the first alarm time
        val startTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, settings.intervalMinutes) // start after interval
        }

        val requestCode = 0
        val intent = Intent(context, NotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = settings.intervalMinutes * 60_000L

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            intervalMillis,
            pi
        )
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
        }
    }
}
