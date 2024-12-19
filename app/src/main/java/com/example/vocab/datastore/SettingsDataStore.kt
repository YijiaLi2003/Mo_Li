package com.example.vocab.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vocab.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore instance
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

// Keys for storing data
object SettingsKeys {
    val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    val NOTIF_START_HOUR = intPreferencesKey("notif_start_hour")
    val NOTIF_END_HOUR = intPreferencesKey("notif_end_hour")
    val NOTIF_INTERVAL_HOURS = intPreferencesKey("notif_interval_hours")
}

// Save notification settings
suspend fun saveNotificationSettings(context: Context, settings: NotificationSettings) {
    context.settingsDataStore.edit { prefs ->
        prefs[SettingsKeys.NOTIF_ENABLED] = settings.enabled
        prefs[SettingsKeys.NOTIF_START_HOUR] = settings.startHour
        prefs[SettingsKeys.NOTIF_END_HOUR] = settings.endHour
        prefs[SettingsKeys.NOTIF_INTERVAL_HOURS] = settings.intervalHours
    }
}

// Load notification settings
fun loadNotificationSettings(context: Context): Flow<NotificationSettings> {
    return context.settingsDataStore.data.map { prefs ->
        NotificationSettings(
            enabled = prefs[SettingsKeys.NOTIF_ENABLED] ?: false,
            startHour = prefs[SettingsKeys.NOTIF_START_HOUR] ?: 7,
            endHour = prefs[SettingsKeys.NOTIF_END_HOUR] ?: 20,
            intervalHours = prefs[SettingsKeys.NOTIF_INTERVAL_HOURS] ?: 2
        )
    }
}
