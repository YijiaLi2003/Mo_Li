package com.example.vocab.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocab.isLandscape
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.Saver

data class Word(val word: String, val phonetic: String, val translation: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavHostController) //, bookName: String, wordList: List<Word>

{
    val isLandscape = isLandscape()
    val coroutineScope = rememberCoroutineScope()
    val bookName = "GRE Core 3000"
    val wordList = listOf(
        Word("Apple", "/ˈæp.l̩/", "苹果"),
        Word("Banana", "/bəˈnæn.ə/", "香蕉"),
        Word("Cherry", "/ˈtʃɛr.i/", "樱桃"),
        Word("Date", "/deɪt/", "海枣"),
        Word("Elderberry", "/ˈɛl.dərˌbɛr.i/", "接骨木莓")
    )

    var currentIndex by rememberSaveable { mutableStateOf(0) }
    val shuffledWords = rememberSaveable(saver = Saver(
        save = { it.map { word -> listOf(word.word, word.phonetic, word.translation) } },
        restore = { it.map { Word(it[0], it[1], it[2]) } }
    )) { wordList.shuffled() }

    if (shuffledWords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No words available", style = MaterialTheme.typography.headlineLarge)
        }
        return
    }

    val currentWord = shuffledWords[currentIndex]

    if (!isLandscape){
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                ,

                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Spacer(modifier = Modifier.weight(0.2f))

                        // Word
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentWord.word,
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        // TODO: Add pronunciation handling logic
                                    }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Add to Favourites",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        // TODO: Add word to favourites list
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.1f))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxSize().padding(vertical = 28.dp).weight(0.2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,

                            ) {
                            // "I Know" Box
                            Box(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .fillMaxWidth().fillMaxHeight()
                                    .padding(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (currentIndex < shuffledWords.size - 1) {
                                            currentIndex++
                                        } else {
                                            // TODO: Handle end of quiz
                                        }
                                    }
                                    .padding(16.dp)
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "I Know",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                )
                            }

                            // "I Forget" Box
                            Box(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .fillMaxWidth().fillMaxHeight()
                                    .padding(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            // TODO: Handle "I Forget" action
                                        }
                                    }
                                    .padding(16.dp)
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "I Forget",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

                    }
                }
            }
        }
    }else{ //in landscape mode



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
                            ),
                        contentAlignment = Alignment.Center,
                    ){
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
                    }

                    // middle button

                    Button(
                        onClick = { navController.popBackStack() },
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
                            .clickable {
                                if (currentIndex < shuffledWords.size - 1) {
                                    currentIndex++
                                } else {
                                    // TODO: Handle end of quiz
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ){
                        if (currentIndex == wordList.size - 1){
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
                            }

                        }


                    }

                }
            }



            // Right Pane (80%)



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
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(0.6f)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                        )
                        {
                            val (word, phonetic, translation) = wordList[currentIndex]


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


                                    Spacer(modifier = Modifier.height(40.dp))


                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.secondary,
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

                                //I Forget box

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
                                        .clickable { /* TODO: handle add to learning word list */ }
                                ) {
                                    Text(
                                        text = "I Forget",
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
                                        .clickable {
                                            if (currentIndex < shuffledWords.size - 1) {
                                                currentIndex++
                                            } else {
                                                // TODO: Handle end of quiz
                                            }
                                        }
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







        }




    }
}