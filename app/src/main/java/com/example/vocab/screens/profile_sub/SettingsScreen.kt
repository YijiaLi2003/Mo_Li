package com.example.vocab.screens.profile_sub

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.model.NotificationSettings
import com.example.vocab.notifications.NotificationScheduler
import com.example.vocab.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settingsFlow.collectAsState()
    var enabled by remember { mutableStateOf(settings.enabled) }
    var startHour by remember { mutableStateOf(settings.startHour) }
    var endHour by remember { mutableStateOf(settings.endHour) }
    var intervalHours by remember { mutableStateOf(settings.intervalHours) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Notifications")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            // Start Hour
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Start Hour: $startHour")
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (startHour > 0) startHour-- }) { Text("-") }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { if (startHour < 23) startHour++ }) { Text("+") }
            }

            // End Hour
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("End Hour: $endHour")
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (endHour > 0) endHour-- }) { Text("-") }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { if (endHour < 23) endHour++ }) { Text("+") }
            }

            // Interval
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Interval (hours): $intervalHours")
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (intervalHours > 1) intervalHours-- }) { Text("-") }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { if (intervalHours < 24) intervalHours++ }) { Text("+") }
            }

            Button(onClick = {
                val newSettings = NotificationSettings(
                    enabled = enabled,
                    startHour = startHour,
                    endHour = endHour,
                    intervalHours = intervalHours
                )
                settingsViewModel.updateSettings(newSettings)
                // Schedule or cancel notifications
                val scheduler = NotificationScheduler()
                scheduler.cancelAll(navController.context)
                if (enabled) {
                    scheduler.scheduleNotifications(navController.context, newSettings)
                }

                Toast.makeText(navController.context, "Settings saved", Toast.LENGTH_SHORT).show()
            }) {
                Text("Save")
            }

        }
    }
}
