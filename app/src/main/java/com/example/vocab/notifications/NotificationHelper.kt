package com.example.vocab.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vocab.R

object NotificationHelper {
    private const val CHANNEL_ID = "memory_refresh_channel"

    fun showNotification(context: Context, title: String, message: String) {
        val nm = NotificationManagerCompat.from(context)

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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you have a proper icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Check notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, skip sending notification
                return
            }
        }

        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
    }
}
