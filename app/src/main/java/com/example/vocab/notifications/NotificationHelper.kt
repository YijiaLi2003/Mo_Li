/**
 * NotificationHelper.kt
 *
 * Provides utility functions for displaying notifications to the user. This helper manages
 * notification channels and ensures notifications are properly displayed, handling permissions
 * where necessary.
 */

package com.example.vocab.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vocab.R

/**
 * NotificationHelper
 *
 * An object that encapsulates methods for creating and displaying notifications.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "memory_refresh_channel" // Unique ID for the notification channel.

    /**
     * Displays a notification to the user.
     *
     * @param context The application context used to access system resources.
     * @param title The title of the notification.
     * @param message The body content of the notification.
     *
     * This method ensures that:
     * - A notification channel is created for devices running Android O (API 26) and above.
     * - Notification permissions are checked for devices running Android 13 (API 33) and above.
     * - The notification is displayed with the provided title and message.
     *
     * If the required notification permissions are not granted, the method logs an error and exits gracefully.
     */
    fun showNotification(context: Context, title: String, message: String) {
        val nm = NotificationManagerCompat.from(context)

        // Check if notification permission is granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NotificationHelper", "Notification permission not granted")
            return
        }

        // Create a notification channel for devices running Android O (API 26) and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Memory Refresh",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to review a wrong word"
                enableLights(true)
                lightColor = Color.GREEN
            }
            nm.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Icon displayed with the notification
            .setContentTitle(title) // Title of the notification
            .setContentText(message) // Body of the notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priority level for the notification

        // Additional permission check for Android Tiramisu (API 33) and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // Display the notification
        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
    }
}
