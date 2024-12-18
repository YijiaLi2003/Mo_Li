package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.isLandscape
import com.example.vocab.ui.theme.LearnInLandscape
import com.example.vocab.ui.theme.LearnInPortrait
import com.example.vocab.ui.theme.Quiz
import com.example.vocab.viewmodel.LearningSectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSection(
    navController: NavHostController,
    learningViewModel: LearningSectionViewModel
) {
    val bookName by learningViewModel.bookName.collectAsState()
    val progressPercentage by learningViewModel.progressPercentage.collectAsState()
    val loading by learningViewModel.loading.collectAsState()
    val desiredCount by learningViewModel.desiredWordCount.collectAsState()
    val currentWords by learningViewModel.words.collectAsState()
    val scrollState = rememberScrollState()
    val isLandscape = isLandscape()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = bookName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(modifier = Modifier.height(36.dp))

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // If no set chosen yet or user finished the set (words empty), show selection
                if (desiredCount == null || currentWords.isEmpty()) {
                    Text(
                        text = "How many words do you want to learn?",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val counts = listOf(10, 20, 30, 40, 50)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        counts.forEach { count ->
                            Button(
                                onClick = {
                                    learningViewModel.setDesiredWordCount(count)
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("$count")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Quiz button
                    CenteredOption(
                        icon = Icons.Outlined.Quiz,
                        text = "Quiz",
                        onClick = { navController.navigate(Quiz.QuizTaking.route) }
                    )
                } else {
                    // A set has been chosen and not finished
                    Spacer(modifier = Modifier.height(36.dp))

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CenteredOption(
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                text = "Continue Learning",
                                modifier = Modifier.weight(0.5f),
                                onClick = {
                                    navController.navigate(LearnInLandscape.LandScapeLearn.route)
                                }
                            )
                            CenteredOption(
                                icon = Icons.Outlined.Quiz,
                                text = "Quiz",
                                modifier = Modifier.weight(0.5f),
                                onClick = {
                                    navController.navigate(Quiz.QuizTaking.route)
                                }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CenteredOption(
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                text = "Continue Learning",
                                modifier = Modifier.weight(0.5f),
                                onClick = {
                                    navController.navigate(LearnInPortrait.PortraitLearn.route)
                                }
                            )
                            CenteredOption(
                                icon = Icons.Outlined.Quiz,
                                text = "Quiz",
                                modifier = Modifier.weight(0.5f),
                                onClick = {
                                    navController.navigate(Quiz.QuizTaking.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CenteredOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(25.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
