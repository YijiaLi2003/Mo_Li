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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.model.UserProgress
import com.example.vocab.viewmodel.LearningSectionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    val userProgress = UserProgress(userId = userId, currentIndex = currentIndex)
    userProgressDao.insertUserProgress(userProgress)
    println("Saving progress locally: $currentIndex")
}

@Composable
fun LearningInLandScreen(
    navController: NavHostController,
    learningViewModel: LearningSectionViewModel = viewModel()
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
                // Top Box (Previous Word)
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
                    if (words.isEmpty() || currentIndex == 0) {
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        val prevIndex = (currentIndex - 1).coerceAtLeast(0)
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
                        }
                    }
                }

                // Middle button (Back)
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

                // Lower box (Next Word)
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
                            if (words.isNotEmpty() && currentIndex < words.size - 1) {
                                currentIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (words.isEmpty() || currentIndex == words.size - 1) {
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        val nextIndex = (currentIndex + 1).coerceAtMost(words.size - 1)
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
                        }
                    }
                }
            }
        }

        // Right Pane (80%)
        if (!showDetails) {
            // Default view
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
                                            trackColor = MaterialTheme.colorScheme.onBackground.copy(
                                                alpha = 0.3f
                                            ),
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
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clickable {
                                                        // TODO Handle Pronunciation API if any
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
                                                        // TODO Handle save to favourite word list if any
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
                                                // Update status to "learning"
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
                                                    currentIndex =
                                                        (currentIndex + 1).coerceAtMost(words.size - 1)
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
                    }
                }
            }

        } else {
            // Details page
            val wordsState = words
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.8f)
            ) {
                val pagerState = rememberPagerState()

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
            }
        }
    }
}