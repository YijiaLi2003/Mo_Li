package com.example.vocab.screens.learn_landscape

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.LearningSectionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.MediaPlayer
import java.io.File

fun playAudioFromFile(file: File) {
    val mediaPlayer = MediaPlayer()
    mediaPlayer.setDataSource(file.absolutePath)
    mediaPlayer.prepare()
    mediaPlayer.start()
}


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
    println("Saving progress locally: $currentIndex")
}

@Composable
fun LearningInLandScreen(
    navController: NavHostController,
    learningViewModel: LearningSectionViewModel
) {
    val context = LocalContext.current
    val bookName by learningViewModel.bookName.collectAsState()
    val progressPercentage by learningViewModel.progressPercentage.collectAsState()
    val words by learningViewModel.words.collectAsState()
    val loading by learningViewModel.loading.collectAsState()

    var showDetails by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
        // User finished the set
        coroutineScope.launch {
            learningViewModel.resetLearningSet()
        }
        // Reset currentIndex to 0 after finishing the set
        currentIndex = 0
        navController.navigate(Screen.LearningSection.route) {
            popUpTo(Screen.LearningSection.route) { inclusive = true }
        }
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
                Button(
                    onClick = {
                        if (context is Activity) {
                            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                        navController.popBackStack()
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
                                    }
                                    currentIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
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
        if (!showDetails) {
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
                        Text("No words available", color = MaterialTheme.colorScheme.secondary)
                    }

                    else -> {
                        if (currentIndex in words.indices) {
                            val currentWordItem = words[currentIndex]
                            Row {
                                // Left part
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.6f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Text(
                                                text = bookName,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.align(Alignment.Start)
                                            )

                                            // Progress Bar
                                            LinearProgressIndicator(
                                                progress = { progressPercentage },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                            )

                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${(progressPercentage * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    ),
                                                    modifier = Modifier.align(Alignment.BottomEnd)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(40.dp))

                                            Text(
                                                text = currentWordItem.word,
                                                style = MaterialTheme.typography.headlineLarge,
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
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                                    contentDescription = "Pronunciation",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(36.dp).clickable {
                                                        val file = File(context.filesDir, "${currentWordItem.word}.mp3")
                                                        if (file.exists()) {
                                                            playAudioFromFile(file)
                                                        } else {
                                                            // File not cached, optionally fetch again or show error
                                                        }
                                                    }
                                                )

                                                Spacer(modifier = Modifier.width(16.dp))
                                                Icon(
                                                    imageVector = Icons.Outlined.StarOutline,
                                                    contentDescription = "Favourite",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clickable {
                                                            learningViewModel.toggleFavorite(currentWordItem.wordId)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right part
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.4f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Spacer(modifier = Modifier.weight(0.15f))

                                        // Learn box (status -> learning)
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 32.dp)
                                                .fillMaxWidth()
                                                .weight(0.3f)
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
                                                    }
                                                    showDetails = true
                                                }
                                        ) {
                                            Text(
                                                text = "Learn",
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    color = MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(0.1f))

                                        // I Know box (status -> mastered)
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 32.dp)
                                                .fillMaxWidth()
                                                .weight(0.3f)
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
                                                    }
                                                    if (words.isNotEmpty() && currentIndex < words.size - 1) {
                                                        currentIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
                                                    } else {
                                                        // Last word completed
                                                        finishSet()
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = "I Know",
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    color = MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(0.15f))
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

        } else {
            // Details page (showDetails == true)
            val wordsState = words
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.8f)
            ) {
                val pagerState = rememberPagerState()

                if (wordsState.isNotEmpty()) {
                    HorizontalPager(
                        count = wordsState.size,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val currentWordItem = wordsState[page]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentWordItem.word,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentWordItem.translation,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                } else {
                    Text("No words available", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}