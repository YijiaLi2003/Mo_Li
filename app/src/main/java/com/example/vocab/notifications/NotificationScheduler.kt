package com.example.vocab.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.vocab.model.NotificationSettings
import java.util.*

class NotificationScheduler {
    fun scheduleNotifications(context: Context, settings: NotificationSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel old alarms first
        cancelAll(context)

        if (!settings.enabled) return

        // Calculate the times and set alarms
        val now = Calendar.getInstance()
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Schedule alarms at each interval until endHour
        var alarmTime = startTime.clone() as Calendar
        while (alarmTime.get(Calendar.HOUR_OF_DAY) <= settings.endHour) {
            val requestCode = alarmTime.get(Calendar.HOUR_OF_DAY) // unique per hour
            val intent = Intent(context, NotificationReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                settings.intervalHours * 60 * 60 * 1000L,
                pi
            )

            alarmTime.add(Calendar.HOUR_OF_DAY, settings.intervalHours)
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (code in 0..23) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                alarmManager.cancel(pi)
            }
        }
    }
}
