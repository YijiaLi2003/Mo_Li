package com.example.vocab.screens.learn_landscape

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.vocab.screens.learn_portrait.playAudioFromFile
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.LearningSectionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


private suspend fun fetchProgressFromDatabase(context: android.content.Context): Int? {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val db = com.example.vocab.database.AppDatabase.getDatabase(context)
    val userProgressDao = db.userProgressDao()
    val userProgress = userProgressDao.getUserProgress(userId)
    return userProgress?.currentIndex
}

private suspend fun saveProgressToDatabase(context: android.content.Context, currentIndex: Int) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = com.example.vocab.database.AppDatabase.getDatabase(context)
    val userProgressDao = db.userProgressDao()
    val userProgress = com.example.vocab.model.UserProgress(userId = userId, currentIndex = currentIndex)
    userProgressDao.insertUserProgress(userProgress)
    Log.d("LearningInLandScreen", "Saving progress locally: $currentIndex")
}

@Composable
fun LearningInLandScreen(
    navController: NavHostController,
    learningViewModel: LearningSectionViewModel
) {
    val context = LocalContext.current
    val words by learningViewModel.words.collectAsState()
    val loading by learningViewModel.loading.collectAsState()
    val detailedInfo by learningViewModel.detailedInfo.collectAsState()

    var showDetails by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Enforce landscape orientation
    if (context is Activity) {
        SideEffect {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val savedIndex = fetchProgressFromDatabase(context)
            withContext(Dispatchers.Main) {
                currentIndex = savedIndex ?: 0
                Log.d("LearningInLandScreen", "Loaded savedIndex: $currentIndex")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch(Dispatchers.IO) {
                saveProgressToDatabase(context, currentIndex)
            }
            if (context is Activity) {
                context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    fun finishSet() {
        coroutineScope.launch {
            learningViewModel.resetLearningSet()
            Log.d("LearningInLandScreen", "Learning set reset.")
        }
        currentIndex = 0
        navController.navigate(Screen.LearningSection.route) {
            popUpTo(Screen.LearningSection.route) { inclusive = true }
        }
        Log.d("LearningInLandScreen", "Navigated back to LearningSection.")
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left Pane (20%)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Previous Word Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.43f)
                        .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            showDetails = false
                            if (words.isNotEmpty() && currentIndex > 0) {
                                currentIndex = (currentIndex - 1).coerceAtLeast(0)
                                Log.d("LearningInLandScreen", "Moved to previous word index: $currentIndex")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (words.isNotEmpty() && currentIndex > 0) {
                        val prevIndex = (currentIndex - 1)
                        val prevWordItem = words.getOrNull(prevIndex)
                        if (prevWordItem != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardDoubleArrowUp,
                                    contentDescription = "Previous",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prevWordItem.word,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(" ", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Back Button
                Button(
                    onClick = {
                        if (context is Activity) {
                            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                        navController.popBackStack()
                        Log.d("LearningInLandScreen", "Back button clicked. Navigating back.")
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.14f)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Next Word Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.43f)
                        .padding(start = 8.dp, end = 4.dp, bottom = 8.dp, top = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            showDetails = false
                            if (words.isNotEmpty()) {
                                if (currentIndex < words.size - 1) {
                                    // Update the current word to 'learning' before moving on
                                    val currentWordItem = words[currentIndex]
                                    coroutineScope.launch {
                                        learningViewModel.updateWordStatus(
                                            currentWordItem.wordId,
                                            "learning"
                                        )
                                        Log.d("LearningInLandScreen", "Marked ${currentWordItem.word} as learning.")
                                    }
                                    currentIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
                                    Log.d("LearningInLandScreen", "Moved to next word index: $currentIndex")
                                } else {
                                    // Last word, user tries to go next -> finish the set
                                    finishSet()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (words.isNotEmpty() && currentIndex < words.size - 1) {
                        val nextIndex = (currentIndex + 1)
                        val nextWordItem = words.getOrNull(nextIndex)
                        if (nextWordItem != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardDoubleArrowDown,
                                    contentDescription = "Next",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = nextWordItem.word,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(" ", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        // If last word and user tries to go next
                        if (words.isNotEmpty() && currentIndex == words.size - 1) {
                            Text(" ", color = MaterialTheme.colorScheme.secondary)
                        } else {
                            // No words at all
                            Text(" ", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Right Pane (80%)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.8f)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> {
                    CircularProgressIndicator()
                }

                words.isEmpty() -> {
                    Text(
                        "No words available",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                else -> {
                    if (currentIndex in words.indices) {
                        val currentWordItem = words[currentIndex]

                        if (!showDetails) {
                            // Main View: Current Word and Translation
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentWordItem.word,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = currentWordItem.translation,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                        contentDescription = "Pronunciation",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                val file = File(context.filesDir, "${currentWordItem.word}.mp3")
                                                if (file.exists()) {
                                                    playAudioFromFile(file)
                                                    Log.d("LearningInLandScreen", "Playing audio for ${currentWordItem.word}")
                                                } else {
                                                    Log.e("LearningInLandScreen", "Audio file not found for ${currentWordItem.word}")
                                                    // Optionally, show a Toast or Snackbar to notify the user
                                                }
                                            }
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Icon(
                                        imageVector = if (currentWordItem.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Favourite",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                coroutineScope.launch {
                                                    learningViewModel.toggleFavorite(currentWordItem.wordId)
                                                    Log.d("LearningInLandScreen", "Toggled favorite for ${currentWordItem.word}")
                                                }
                                            }
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Learn and I Know Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Learn Button
                                    Box(
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .height(80.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .border(
                                                width = 3.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    learningViewModel.updateWordStatus(
                                                        currentWordItem.wordId,
                                                        "learning"
                                                    )
                                                    Log.d("LearningInLandScreen", "Marked ${currentWordItem.word} as learning.")
                                                    learningViewModel.loadDetailedInfo(currentWordItem.wordId)
                                                    Log.d("LearningInLandScreen", "Loading detailed info for ${currentWordItem.word}")
                                                }
                                                showDetails = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Learn",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                color = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // I Know Button
                                    Box(
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .height(80.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .border(
                                                width = 3.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    learningViewModel.updateWordStatus(
                                                        currentWordItem.wordId,
                                                        "mastered"
                                                    )
                                                    Log.d("LearningInLandScreen", "Marked ${currentWordItem.word} as mastered.")
                                                }
                                                if (words.isNotEmpty() && currentIndex < words.size - 1) {
                                                    currentIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
                                                    Log.d("LearningInLandScreen", "Moved to next word index: $currentIndex")
                                                } else {
                                                    // Last word completed
                                                    finishSet()
                                                    Log.d("LearningInLandScreen", "Finished learning set.")
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "I Know",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                color = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Detailed Info View
                            if (detailedInfo == null) {
                                // Still loading detailed info
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Loading detailed info...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            } else {
                                // Display detailed info
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Close Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = { showDetails = false }) {
                                            Icon(
                                                imageVector = Icons.Outlined.KeyboardDoubleArrowUp,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Word
                                    Text(
                                        text = currentWordItem.word,
                                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Chinese Translation
                                    Text(
                                        text = detailedInfo!!.translation,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Phonetic
                                    Text(
                                        text = detailedInfo!!.phonetic?.let { "Phonetic: $it" } ?: "Phonetic: N/A",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Examples
                                    if (detailedInfo!!.examples.isNotEmpty()) {
                                        Text(
                                            text = "Examples:",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Make the examples scrollable if they are too many
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 200.dp)
                                        ) {
                                            detailedInfo!!.examples.forEach { example ->
                                                Text(
                                                    text = "- $example",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "No example sentences found.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // If currentIndex not in range or no words
                        Text("No words available", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
