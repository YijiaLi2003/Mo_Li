/**
 * NotificationSettingsDataStore.kt
 *
 * Provides utility functions to manage notification settings using Jetpack DataStore.
 * DataStore is used to persist user preferences such as notification settings and dark mode state.
 */

package com.example.vocab.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vocab.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define a DataStore instance tied to the application's context for storing preferences.
private val Context.settingsDataStore by preferencesDataStore(name = "notification_settings")

// Keys used for storing and retrieving preferences from DataStore.
private val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
private val KEY_INTERVAL = intPreferencesKey("notifications_interval_minutes")
private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")

/**
 * Loads the notification settings from DataStore as a Flow.
 *
 * @param context The application context used to access the DataStore instance.
 * @return A Flow that emits the current NotificationSettings object whenever the data changes.
 */
fun loadNotificationSettings(context: Context): Flow<NotificationSettings> {
    return context.settingsDataStore.data.map { prefs ->
        NotificationSettings(
            enabled = prefs[KEY_ENABLED] ?: false,
            intervalMinutes = prefs[KEY_INTERVAL] ?: 60,
            darkMode = prefs[KEY_DARK_MODE] ?: false
        )
    }
}

/**
 * Saves the notification settings to DataStore.
 *
 * @param context The application context used to access the DataStore instance.
 * @param settings The NotificationSettings object containing user preferences.
 */
suspend fun saveNotificationSettings(context: Context, settings: NotificationSettings) {
    context.settingsDataStore.edit { prefs ->
        prefs[KEY_ENABLED] = settings.enabled
        prefs[KEY_INTERVAL] = settings.intervalMinutes
        prefs[KEY_DARK_MODE] = settings.darkMode
    }
}
