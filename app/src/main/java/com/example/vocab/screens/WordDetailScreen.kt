package com.example.vocab.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vocab.viewmodel.PronunciationViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

@Composable
fun WordDetailScreen(word: String) {
    val pronunciationViewModel: PronunciationViewModel = viewModel()
    val audioUrlResult by pronunciationViewModel.audioUrl.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Word: $word", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            pronunciationViewModel.fetchPronunciation(word)
        }) {
            Text("Hear Pronunciation")
        }

        // Handle UI states
        audioUrlResult?.let { result ->
            if (result.isSuccess) {
                val audioUrl = result.getOrNull()
                if (audioUrl != null) {
                    // Play the audio
                    PlayAudio(url = audioUrl)
                    // Reset the state after playing
                    pronunciationViewModel.reset()
                }
            } else if (result.isFailure) {
                Text("Error: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}

@Composable
fun PlayAudio(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
