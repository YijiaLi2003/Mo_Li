package com.example.vocab.screens.learn_landscape

import android.app.Activity
import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.viewmodel.LearningSectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset


private fun fetchProgressFromDatabase(): Int? {

    // TODO: Fetch the saved progress from the database
    // Simulate database fetch with null (no progress stored yet)
    return null
}


private fun saveProgressToDatabase(currentIndex: Int) {

    // TODO: Save the current index to the database
    println("Saving progress: $currentIndex") // Simulated save
}

@Composable
fun LearningInLandScreen(navController: NavHostController, learningViewModel: LearningSectionViewModel = viewModel()) {
    val context = LocalContext.current
    val bookName by learningViewModel.bookName.collectAsState()
    val progressPercentage by learningViewModel.progressPercentage.collectAsState()
    var showDetails by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Enforce landscape orientation
    if (context is Activity) {
        context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    // Hardcoded list of words!!!!!!!!!!!!!!
    val words = listOf(
        Triple("Apple", "/ˈæp.l̩/", "苹果"),
        Triple("Banana", "/bəˈnæn.ə/", "香蕉"),
        Triple("Cherry", "/ˈtʃɛr.i/", "樱桃"),
        Triple("Date", "/deɪt/", "海枣"),
        Triple("Elderberry", "/ˈɛl.dərˌbɛr.i/", "接骨木莓")
    )
    var currentIndex by rememberSaveable { mutableIntStateOf(0) } //!!!!!!!!!!!!!!!!

    // Load the initial progress (simulate database fetch)
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            // TODO: Replace this with a database fetch function
            currentIndex = fetchProgressFromDatabase() ?: 0
        }
    }

    // Enforce landscape orientation
    if (context is Activity) {
        context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch(Dispatchers.IO) {

                // TODO: Replace this with a database save function
                saveProgressToDatabase(currentIndex)
            }

            if (context is Activity) {
                context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    val pagerState = rememberPagerState()
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left Pane (20%)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(0.2f)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Top Box for previous word

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)
                        .weight(0.43f)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { showDetails = false
                            if (currentIndex != 0) {currentIndex = (currentIndex - 1) % words.size} },
                    contentAlignment = Alignment.Center,
                ){
                    if (currentIndex == 0){
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    } else{
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardDoubleArrowUp,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            val (wordPre, phoneticPre, translationPre) = words[currentIndex - 1]
                            Text(
                                text = wordPre,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                }

                // middle button

                Button(
                    onClick = {
                        if (context is Activity) {
                            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp).weight(0.14f).fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // lower box

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 4.dp, bottom = 8.dp, top = 4.dp)
                        .weight(0.43f)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {showDetails = false
                            if (currentIndex != words.size - 1) {currentIndex = (currentIndex + 1) % words.size} },
                    contentAlignment = Alignment.Center,
                ){
                    if (currentIndex == words.size - 1){
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    } else {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardDoubleArrowDown,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            val (wordNext, phoneticNext, translationNext) = words[currentIndex + 1]
                            Text(
                                text = wordNext,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }


                }

            }
        }



        // Right Pane (80%)

        if (!showDetails){
            //default view

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(0.8f)
                    .padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Row{

                    // Right pane left part
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight()
                            .weight(0.6f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            ),
                    )
                    {
                        val (word, phonetic, translation) = words[currentIndex]


                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .fillMaxHeight()
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(text = bookName,
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

                                Spacer(modifier = Modifier.height(40.dp))


                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = phonetic,
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
                                        modifier = Modifier.size(36.dp)
                                            .clickable { /* Handle Pronunciation API here!!!!!!!!!!!!!!! */ }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.StarOutline,
                                        contentDescription = "favourite",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(36.dp)
                                            .clickable { /* Handle save to favourite word list here!!!!!!!!!!!!! */ }
                                    )
                                }
                            }
                        }



                    }

                    //right pane right part

                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(0.4f)
                    ){
                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Spacer(modifier = Modifier.weight(0.15f))

                            //Learn box

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .weight(0.3f)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp),

                                        )
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { showDetails = true }
                            ) {
                                Text(
                                    text = "Learn",
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.1f))

                            //I know box

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
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
                                    .clickable { if (currentIndex != words.size - 1) {currentIndex = (currentIndex + 1) % words.size} }
                            ) {
                                Text(
                                    text = "I Know",
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.15f))

                        }

                    }


                }


            }


        }else{
            // details page

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                HorizontalPager(
                    count = words.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val (word, phonetic, translation) = words[page]
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
                            text = word,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = phonetic,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = translation,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }


        }

    }
}