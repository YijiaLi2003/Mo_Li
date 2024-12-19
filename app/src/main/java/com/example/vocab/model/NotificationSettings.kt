package com.example.vocab.model

data class NotificationSettings(
    val enabled: Boolean = false,
    val startHour: Int = 7, // 7 AM default
    val endHour: Int = 20,  // 8 PM default
    val intervalHours: Int = 2 // every two hours
)
