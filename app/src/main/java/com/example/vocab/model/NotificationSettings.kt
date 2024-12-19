// NotificationSettings.kt
package com.example.vocab.model

data class NotificationSettings(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 60,
    val darkMode: Boolean = false
)
