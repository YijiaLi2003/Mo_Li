package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You\nAre\nWorking\nHard!",
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
        Text(
            text = "Mo_Li Vocab",
            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.align(Alignment.BottomCenter).padding(25.dp)
        )
    }
}

