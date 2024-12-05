package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.vocab.ui.theme.ProfileSubScreen
import com.example.vocab.viewmodel.LearningSectionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSection(navController: NavHostController, learningViewModel: LearningSectionViewModel = viewModel()) {

    val bookName by learningViewModel.bookName.collectAsState()
    val progressPercentage by learningViewModel.progressPercentage.collectAsState()
    val scrollState = rememberScrollState()
    val isLandscape = isLandscape()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = bookName)
                },
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
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = "Pick one to start",
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (isLandscape){

                // Four square rounded texts in a row

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .padding(end = 8.dp)
                            .weight(0.5f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable { navController.navigate(LearnInLandscape.LandScapeLearn.route)},
                        contentAlignment = Alignment.Center,


                        ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = "Book",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Continue Learning",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .padding(start = 8.dp)
                            .weight(0.5f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable { navController.navigate(ProfileSubScreen.FavouriteWords.route) },
                        contentAlignment = Alignment.Center

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Favourite",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Favourite Words",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .padding(end = 8.dp)
                            .weight(0.5f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable { navController.navigate(ProfileSubScreen.ReStudyWords.route) },
                        contentAlignment = Alignment.Center

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = "Review List",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Words Need Review",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .padding(start = 8.dp)
                            .weight(0.5f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable { navController.navigate(ProfileSubScreen.WordNotes.route) },
                        contentAlignment = Alignment.Center

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.Outlined.NoteAlt,
                                contentDescription = "word_notes",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Word Notes",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                }



            } else{
                // Four square rounded texts
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Space between the two rows
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .padding(end = 8.dp)
                                .weight(0.5f)
                                .aspectRatio(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable { navController.navigate(LearnInPortrait.PortraitLearn.route) },
                            contentAlignment = Alignment.Center,


                            ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            )
                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = "Book",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Continue Learning",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .padding(start = 8.dp)
                                .weight(0.5f)
                                .aspectRatio(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable { navController.navigate(ProfileSubScreen.FavouriteWords.route) },
                            contentAlignment = Alignment.Center

                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            )
                            {
                                Icon(
                                    imageVector = Icons.Outlined.StarOutline,
                                    contentDescription = "Favourite",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Favourite Words",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .padding(end = 8.dp)
                                .weight(0.5f)
                                .aspectRatio(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable { navController.navigate(ProfileSubScreen.ReStudyWords.route) },
                            contentAlignment = Alignment.Center

                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            )
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Bookmarks,
                                    contentDescription = "Review List",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Words Need Review",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .padding(start = 8.dp)
                                .weight(0.5f)
                                .aspectRatio(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable { navController.navigate(ProfileSubScreen.WordNotes.route) },
                            contentAlignment = Alignment.Center

                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            )
                            {
                                Icon(
                                    imageVector = Icons.Outlined.NoteAlt,
                                    contentDescription = "word_notes",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Word Notes",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
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