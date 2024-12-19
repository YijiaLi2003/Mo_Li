// SettingsViewModel.kt
package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.datastore.loadNotificationSettings
import com.example.vocab.datastore.saveNotificationSettings
import com.example.vocab.model.NotificationSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    val settingsFlow = loadNotificationSettings(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationSettings()
        )

    fun updateSettings(newSettings: NotificationSettings) {
        viewModelScope.launch {
            saveNotificationSettings(context, newSettings)
        }
    }
}
