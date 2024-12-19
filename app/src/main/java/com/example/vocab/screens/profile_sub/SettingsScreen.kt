// SettingsScreen.kt
package com.example.vocab.screens

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settingsFlow.collectAsState()

    var enabled by remember { mutableStateOf(settings.enabled) }
    var intervalMinutes by remember { mutableStateOf(settings.intervalMinutes) }

    // Whenever settingsFlow changes, update our local UI states
    LaunchedEffect(settings) {
        enabled = settings.enabled
        intervalMinutes = settings.intervalMinutes
    }

    val coroutineScope = rememberCoroutineScope()

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
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Notifications")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            // Interval in minutes
            Text("Interval Minutes: $intervalMinutes")
            Row {
                Button(onClick = { if (intervalMinutes > 1) intervalMinutes-- }) { Text("-") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (intervalMinutes < 1440) intervalMinutes++ }) { Text("+") }
            }

            Button(onClick = {
                val newSettings = NotificationSettings(enabled, intervalMinutes)
                settingsViewModel.updateSettings(newSettings)
                coroutineScope.launch(Dispatchers.IO) {
                    val scheduler = NotificationScheduler()
                    scheduler.cancelAll(navController.context)
                    if (enabled) {
                        scheduler.scheduleNotifications(navController.context, newSettings)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(navController.context, "Settings saved", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}
