package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.MainScreenViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    // Observe ViewModel data
    val userName by mainScreenViewModel.userName.collectAsState()
    val currentWordBook by mainScreenViewModel.currentWordBook.collectAsState()
    val progress by mainScreenViewModel.progress.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.weight(0.2f))
                GreetingSection(userName)
                Spacer(modifier = Modifier.weight(0.1f))
                ProgressCard(currentWordBook, progress)
                Spacer(modifier = Modifier.weight(0.2f))
                StartButton(onStartClick = { navController.navigate(Screen.LearningSection.route) })
                Spacer(modifier = Modifier.weight(0.2f))
            }
        }
    }
}


@Composable
fun GreetingSection(userName: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(vertical = 30.dp)
    ) {
        Text(
            text = "Hi, $userName!",
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
fun ProgressCard(currentWordBook: String, progress: Float) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Book",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp).align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Current Learning:\n$currentWordBook",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            LinearProgressIndicator(
                progress = {
                    progress // Updated to use dynamic progress!!!!!!!!!
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}


@Composable
fun StartButton(onStartClick: () -> Unit) {
    Button(
        onClick = onStartClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .padding(horizontal = 49.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        Text(
            text = "START",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
}