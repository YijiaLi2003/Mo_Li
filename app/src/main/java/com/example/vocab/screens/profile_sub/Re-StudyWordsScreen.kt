package com.example.vocab.screens.profile_sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.viewmodel.ReStudyWordsViewModel
import com.example.vocab.viewmodel.WordItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReStudyWordsScreen(
    navController: NavHostController,
    reStudyWordsViewModel: ReStudyWordsViewModel = viewModel()
) {
    val loading by reStudyWordsViewModel.loading.collectAsState()
    val words by reStudyWordsViewModel.words.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Re-Study Words")
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
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when {
                loading -> {
                    // Show a loading indicator while we fetch data
                    CircularProgressIndicator()
                }
                words.isEmpty() -> {
                    // After loading completes, if no words found, show a message
                    Text(text = "No words available", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                }
                else -> {
                    // Show the list of words using a LazyColumn to handle large sets efficiently
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(words) { wordItem ->
                            WordRow(wordItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordRow(wordItem: WordItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = wordItem.word,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = wordItem.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${wordItem.status}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
