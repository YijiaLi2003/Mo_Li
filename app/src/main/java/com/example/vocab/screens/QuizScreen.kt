// QuizScreen.kt
package com.example.vocab.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.QuizViewModel

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavHostController, quizViewModel: QuizViewModel = viewModel()) {
    val context = LocalContext.current
    val isLandscape = isLandscape()

    val words by quizViewModel.words.collectAsState()
    val currentIndex by quizViewModel.currentIndex.collectAsState()
    val quizFinished by quizViewModel.quizFinished.collectAsState()

    // var to allow type it during the quiz
    var showTypeItDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    //deal with type it pop up message
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    // If quiz finished, navigate to Home.
    LaunchedEffect(quizFinished) {
        if (quizFinished) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    // If no words are available
    if (words.isEmpty()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "Quiz") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        titleContentColor = MaterialTheme.colorScheme.secondary,
                        navigationIconContentColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...", style = MaterialTheme.typography.headlineLarge)
            }
        }
        return
    }

    val currentWord = words.getOrNull(currentIndex)
    val bookName = "TOEFL"

    if (!isLandscape) {
        // Portrait mode layout
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {

                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    // // progress bar
                    val totalWords = words.size
                    val quizProgress = if (totalWords > 0) (currentIndex + 1).toFloat() / totalWords else 0f
                    val percentage = (quizProgress * 100).toInt()
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { quizProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Spacer(modifier = Modifier.weight(0.1f))

                            // Current Word
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentWord?.word ?: "N/A",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Icons for volume and favourite
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
                                            currentWord?.let { word ->
                                                quizViewModel.playAudio(word.word)
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = if (currentWord?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Add to Favourites",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            currentWord?.let { word ->
                                                quizViewModel.toggleFavorite(word.wordId)
                                            }
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.1f))

                            // Action Buttons: Type It/ I Know / I Forget

                            Column(
                                verticalArrangement = Arrangement.Bottom
                            ){

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    // I Know
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                            .aspectRatio(1.618f)
                                            .fillMaxHeight()
                                            .padding(16.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                quizViewModel.onKnow()
                                            }
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

                                    // I Forget
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                            .aspectRatio(1.618f)
                                            .fillMaxHeight()
                                            .padding(16.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                quizViewModel.onForget()
                                            }
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
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(70.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { showTypeItDialog = true }
                                        .border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = "Type It",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                            }


                        }
                    }


                }


            }
        }
        if (showTypeItDialog) {
            TypeItDialog(
                currentWord = currentWord?.word ?: "",
                onDismiss = { showTypeItDialog = false },
                onSubmit = { typedWord ->
                    val correct = typedWord.trim().lowercase() == (currentWord?.word?.lowercase() ?: "")
                    if (correct) {
                        quizViewModel.onKnow()
                        toastMessage = "Correct;)"
                    } else if (typedWord.isBlank()){
                        toastMessage = "Type something please :0"
                    } else {
                        quizViewModel.onForget()
                        toastMessage = "Incorrect spelling:("
                    }
                    showTypeItDialog = false
                }
            )
        }



    } else {
        // Landscape mode layout

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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Previous word box (optional, can implement if needed)
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
                            .clickable {
                                // Optionally implement previous word navigation
                            },
                        contentAlignment = Alignment.Center,
                    ){
                        Text(" ", color = MaterialTheme.colorScheme.secondary)
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
                            .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                            .weight(0.14f)
                            .fillMaxWidth(),
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

                    // Next word box
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
                                // Move to next word after acknowledging knowledge
                                quizViewModel.onKnow()
                            },
                        contentAlignment = Alignment.Center,
                    ){
                        if (currentIndex == words.size - 1){
                            Text(" ", color = MaterialTheme.colorScheme.secondary)
                        } else {
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Text(text = bookName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            //a progress bar to track the current quiz
                            val totalWords = words.size
                            val quizProgress = if (totalWords > 0) (currentIndex + 1).toFloat() / totalWords else 0f
                            val percentage = (quizProgress * 100).toInt()
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { quizProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                )
                            }


                            Text(
                                text = currentWord?.word ?: "N/A",
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
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            currentWord?.let { word ->
                                                quizViewModel.playAudio(word.word)
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = if (currentWord?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Favourite",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            currentWord?.let { word ->
                                                quizViewModel.toggleFavorite(word.wordId)
                                            }
                                        }
                                )
                            }
                        }
                    }

                    // Right pane right part
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(0.4f)
                    ){
                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Spacer(modifier = Modifier.weight(0.1f))

                            // I Forget box
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .weight(0.2f)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp),
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { quizViewModel.onForget() }
                            ) {
                                Text(
                                    text = "I Forget",
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.1f))

                            // type it box
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .weight(0.2f)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { showTypeItDialog = true }
                            ) {
                                Text(
                                    text = "Type It",
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.1f))

                            // I Know box
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .weight(0.2f)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { quizViewModel.onKnow() }
                            ) {
                                Text(
                                    text = "I Know",
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.1f))



                        }

                    }


                }


            }


        }
    }

}

@SuppressLint("RememberReturnType")
@Composable
fun TypeItDialog(currentWord: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var inputText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Type the word", color = MaterialTheme.colorScheme.secondary) },
        text = {
            Column {
                Text(
                    text = "Please type the English word you remember:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSubmit(inputText)
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(inputText) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}