// NotificationSettingsDataStore.kt
package com.example.vocab.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vocab.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "notification_settings")

private val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
private val KEY_INTERVAL = intPreferencesKey("notifications_interval_minutes")

fun loadNotificationSettings(context: Context): Flow<NotificationSettings> {
    return context.settingsDataStore.data.map { prefs ->
        NotificationSettings(
            enabled = prefs[KEY_ENABLED] ?: false,
            intervalMinutes = prefs[KEY_INTERVAL] ?: 60
        )
    }
}

suspend fun saveNotificationSettings(context: Context, settings: NotificationSettings) {
    context.settingsDataStore.edit { prefs ->
        prefs[KEY_ENABLED] = settings.enabled
        prefs[KEY_INTERVAL] = settings.intervalMinutes
    }
}
