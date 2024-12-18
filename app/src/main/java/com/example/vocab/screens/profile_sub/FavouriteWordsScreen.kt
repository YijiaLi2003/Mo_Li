// FavoriteWordsScreen.kt
package com.example.vocab.screens.profile_sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.vocab.viewmodel.FavoriteWordsViewModel
import com.example.vocab.viewmodel.WordItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteWordsScreen(
    navController: NavHostController,
    favoriteWordsViewModel: FavoriteWordsViewModel = viewModel()
) {
    val loading by favoriteWordsViewModel.loading.collectAsState()
    val favoriteWords by favoriteWordsViewModel.favoriteWords.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Favorite Words")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                loading -> {
                    // Show a loading indicator while data is being fetched
                    CircularProgressIndicator()
                }
                favoriteWords.isEmpty() -> {
                    // Show a message if there are no favorite words
                    Text(
                        text = "No favorite words.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                else -> {
                    // Display the list of favorite words using LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favoriteWords) { wordItem ->
                            FavoriteWordRow(wordItem = wordItem, onToggleFavorite = {
                                favoriteWordsViewModel.toggleFavorite(wordItem.wordId)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteWordRow(wordItem: WordItem, onToggleFavorite: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (wordItem.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (wordItem.isFavorite) "Unfavorite" else "Favorite",
                    tint = if (wordItem.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
