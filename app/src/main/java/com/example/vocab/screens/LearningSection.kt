package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.isLandscape
import com.example.vocab.ui.theme.LearnInLandscape
import com.example.vocab.ui.theme.LearnInPortrait
import com.example.vocab.ui.theme.Quiz
import com.example.vocab.ui.theme.Shapes
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
                if (desiredCount == null ) {
                    Text(
                        text = "Select the Number of Words You Want to Learn.",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    val counts = listOf(10, 20, 30, 40, 50)
                    if (isLandscape){

                        // landscape mode

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            counts.forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                        )
                                        .border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ).clickable {
                                            learningViewModel.setDesiredWordCount(count)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            // Quiz button

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .clickable { navController.navigate(Quiz.QuizTaking.route) }
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ){
                                    Icon(
                                        imageVector = Icons.Outlined.Quiz,
                                        contentDescription = "Quiz",
                                        modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "Quiz",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }
                        }



                    } else {

                        //  portrait mode
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            counts.forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(20.dp)
                                        ).clickable {
                                            learningViewModel.setDesiredWordCount(count)
                                        }
                                ) {
                                    Text(
                                        text = "$count",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        // Quiz button
                        Box(
                            modifier = Modifier.height(80.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { navController.navigate(Quiz.QuizTaking.route) }
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                        ){
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                            ){
                                Icon(
                                    imageVector = Icons.Outlined.Quiz,
                                    contentDescription = "Quiz",
                                    modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = "Quiz",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )

                            }
                        }

                    }





                } else {
                    // A set has been chosen and not finished
                    Spacer(modifier = Modifier.height(36.dp))

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            //continue learning button
                            Box(
                                modifier = Modifier.height(80.dp)
                                    .weight(0.4f)
                                    .padding(horizontal = 16.dp)
                                    .clickable { navController.navigate(LearnInLandscape.LandScapeLearn.route) }
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ){
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                        contentDescription = "Continue learning",
                                        modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "Continue Learning",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }
                            Spacer(modifier = Modifier.weight(0.1f))
                            // quiz button
                            Box(
                                modifier = Modifier.height(80.dp)
                                    .weight(0.4f)
                                    .padding(horizontal = 16.dp)
                                    .clickable { navController.navigate(Quiz.QuizTaking.route) }
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ){
                                    Icon(
                                        imageVector = Icons.Outlined.Quiz,
                                        contentDescription = "Quiz",
                                        modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "Quiz",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }
                        }
                    } else {

                        // portrait mode learning section screen


                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            //continue learning button
                            Box(
                                modifier = Modifier.height(80.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickable { navController.navigate(LearnInPortrait.PortraitLearn.route) }
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ){
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                        contentDescription = "Continue learning",
                                        modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "Continue Learning",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }

                            Spacer(modifier = Modifier.height(36.dp))

                            // quiz button
                            Box(
                                modifier = Modifier.height(80.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickable { navController.navigate(Quiz.QuizTaking.route) }
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ){
                                    Icon(
                                        imageVector = Icons.Outlined.Quiz,
                                        contentDescription = "Quiz",
                                        modifier = Modifier.size(36.dp).align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = "Quiz",
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
        }
    }
}
