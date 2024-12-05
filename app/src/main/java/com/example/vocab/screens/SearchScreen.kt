package com.example.vocab.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vocab.viewmodel.SearchViewModel
import com.example.vocab.viewmodel.WordItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val query by searchViewModel.query.collectAsState()
    val statusFilter by searchViewModel.statusFilter.collectAsState()
    val loading by searchViewModel.loading.collectAsState()
    val results by searchViewModel.results.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Dictionary Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { searchViewModel.onQueryChange(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { searchViewModel.onStatusFilterChange(it) }
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    loading -> {
                        CircularProgressIndicator()
                    }
                    results.isEmpty() -> {
                        Text("No words match your search", style = MaterialTheme.typography.bodyLarge)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(results) { wordItem ->
                                WordResultRow(wordItem)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            label = { Text("Search for a word (Chinese or English)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter (Dropdown or Row of Buttons)
        // For simplicity, let's do a row of RadioButtons:
        val options = listOf("all", "unseen", "learning", "mastered")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (statusFilter == option),
                        onClick = { onStatusFilterChange(option) }
                    )
                    Text(text = option.capitalize())
                }
            }
        }
    }
}

@Composable
fun WordResultRow(wordItem: WordItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = wordItem.word, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = wordItem.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${wordItem.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
