// LearningInPortScreen.kt
package com.example.vocab.screens.learn_portrait

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.model.UserProgress
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.LearningSectionViewModel
import com.google.firebase.auth.FirebaseAuth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningInPortScreen(
    navController: NavHostController,
    learningViewModel: LearningSectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val bookName by learningViewModel.bookName.collectAsState()
    val progressPercentage by learningViewModel.progressPercentage.collectAsState()
    val words by learningViewModel.words.collectAsState()
    val loading by learningViewModel.loading.collectAsState()

    // State for currentIndex
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Learn button can trigger the content to show up in middle section
    var showLearnContent by rememberSaveable { mutableStateOf(false) }

    // Helper function to finish the learning set
    fun finishSet() {
        coroutineScope.launch {
            learningViewModel.resetLearningSet()
        }
        // Reset currentIndex to 0 after finishing the set
        currentIndex = 0
        navController.navigate(Screen.LearningSection.route) {
            popUpTo(Screen.LearningSection.route) { inclusive = true }
        }
    }

    // Enforce portrait orientation
    if (context is Activity) {
        SideEffect {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Load progress on launch
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val savedIndex = fetchProgressFromDatabase(context)
            withContext(Dispatchers.Main) {
                currentIndex = savedIndex ?: 0
            }
        }
    }

    // Save progress on dispose
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bookName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${(progressPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = {
                                    progressPercentage // Correct: Float value
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.3f
                                ),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        // Main content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                    // Main Content
                    val currentWord = words.getOrNull(currentIndex)

                    if (currentWord == null) {
                        Text(
                            "Invalid word index",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // Top Area: Current Word
                            Box(
                                modifier = Modifier.weight(0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentWord.word,
                                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                            contentDescription = "Pronunciation",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    // TODO: Handle Pronunciation API if any
                                                }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Icon(
                                            imageVector = if (currentWord.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                            contentDescription = "Favourite",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    learningViewModel.toggleFavorite(currentWord.wordId)
                                                }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Middle Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.65f)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        // Update status to "learning"
                                        coroutineScope.launch {
                                            learningViewModel.updateWordStatus(
                                                currentWord.wordId,
                                                "learning"
                                            )
                                        }
                                        // Show learn content
                                        showLearnContent = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (showLearnContent) {
                                    // Detailed Learn Content once "Learn" is clicked
                                    Text(
                                        text = "Detailed Learn Content TODO!!!!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    // Default eye icon
                                    Icon(
                                        imageVector = Icons.Outlined.RemoveRedEye,
                                        contentDescription = "Tap to see",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom area: "I Know" and "Learn" or "Next"
                            Box(
                                modifier = Modifier.weight(0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // "I Know" box
                                    Box(
                                        modifier = Modifier
                                            .weight(0.3f)
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
                                                        currentWord.wordId,
                                                        "mastered"
                                                    )
                                                }
                                                if (words.isNotEmpty() && currentIndex < words.size - 1) {
                                                    currentIndex =
                                                        (currentIndex + 1).coerceAtMost(words.size - 1)
                                                } else {
                                                    // Last word completed
                                                    finishSet()
                                                }

                                                showLearnContent = false
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
                                    Spacer(modifier = Modifier.weight(0.1f))
                                    // Show "Next" if learn content is shown, else show "Learn"
                                    if (showLearnContent) {
                                        Box(
                                            modifier = Modifier
                                                .weight(0.3f)
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
                                                    if (words.isNotEmpty() && currentIndex < words.size - 1) {
                                                        currentIndex =
                                                            (currentIndex + 1).coerceAtMost(words.size - 1)
                                                    } else {
                                                        // Last word completed
                                                        finishSet()
                                                    }

                                                    showLearnContent = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Next",
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    color = MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(0.3f)
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
                                                    // Update status to "learning"
                                                    coroutineScope.launch {
                                                        learningViewModel.updateWordStatus(
                                                            currentWord.wordId,
                                                            "learning"
                                                        )
                                                    }
                                                    // Show learn content
                                                    showLearnContent = true
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
