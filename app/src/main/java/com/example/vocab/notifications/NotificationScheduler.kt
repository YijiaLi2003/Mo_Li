/**
 * NotificationScheduler.kt
 *
 * This class is responsible for scheduling and managing notifications using Android's AlarmManager.
 * It allows notifications to be scheduled at regular intervals based on user-defined settings.
 */

package com.example.vocab.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vocab.model.NotificationSettings
import java.util.*

/**
 * NotificationScheduler
 *
 * A utility class for scheduling and canceling notifications using the AlarmManager.
 */
class NotificationScheduler {

    private val TAG = "NotificationScheduler"

    /**
     * Schedules repeating notifications based on the provided settings.
     *
     * @param context The application context.
     * @param settings The notification settings containing interval and enable state.
     */
    fun scheduleNotifications(context: Context, settings: NotificationSettings) {
        if (!settings.enabled) {
            Log.d(TAG, "Notifications are disabled in settings. No notifications scheduled.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelAll(context) // Ensure no duplicate alarms are scheduled.

        // Calculate the start time for the notifications
        val startTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, settings.intervalMinutes)
        }
        val requestCode = 0
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = settings.intervalMinutes * 60_000L

        // Schedule the repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            intervalMillis,
            pendingIntent
        )
        Log.d(TAG, "Scheduled notifications to start at ${startTime.time} with an interval of $intervalMillis ms.")
    }

    /**
     * Cancels all scheduled notifications for this app.
     *
     * @param context The application context.
     */
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "All scheduled notifications have been canceled.")
        } else {
            Log.d(TAG, "No scheduled notifications to cancel.")
        }
    }
}
