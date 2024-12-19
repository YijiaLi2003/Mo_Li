// SettingsScreen.kt
package com.example.vocab.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.model.NotificationSettings
import com.example.vocab.notifications.NotificationScheduler
import com.example.vocab.ui.theme.Screen
import com.example.vocab.ui.theme.SettingsSubScreen
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

    var darkModeEnabled by remember { mutableStateOf(settings.darkMode) }
    // Update local states whenever settingsFlow changes
    LaunchedEffect(settings) {
        enabled = settings.enabled
        darkModeEnabled = settings.darkMode
    }

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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
                )
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
            //notification entry
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Enable Notifications",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            // Update settings immediately
                            val newSettings = settings.copy(enabled = enabled, darkMode = darkModeEnabled)
                            settingsViewModel.updateSettings(newSettings)
                        }
                    )
                }
            }

            // If notifications enabled, show a button to navigate to interval settings
            if (enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            // Navigate to a separate notification settings screen
                            navController.navigate(SettingsSubScreen.NotificationSub.route)
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Notification Interval Settings",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Dark Mode Entry
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = {
                            darkModeEnabled = it
                            val newSettings = settings.copy(darkMode = darkModeEnabled, enabled = enabled)
                            settingsViewModel.updateSettings(newSettings)
                        }
                    )
                }
            }
        }
    }
}
