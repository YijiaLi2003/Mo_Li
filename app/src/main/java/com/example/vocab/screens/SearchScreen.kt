package com.example.vocab.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vocab.viewmodel.SearchViewModel
import com.example.vocab.viewmodel.WordItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val query by searchViewModel.query.collectAsState()
    val statusFilter by searchViewModel.statusFilter.collectAsState()
    val loading by searchViewModel.loading.collectAsState()
    val results by searchViewModel.results.collectAsState()
    val landscape = isLandscape()

    // Track if a search was triggered (icon click or Enter key)
    var searchTriggered by rememberSaveable { mutableStateOf(false) }

    // If filter changes and search already triggered and query not blank, update results instantly
    LaunchedEffect(statusFilter) {
        if (searchTriggered && query.isNotBlank()) {
            searchViewModel.performSearch()
        }
    }

    if (!landscape){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Box instead of top app bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(25.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Search | 查询",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                )
            }

            // The rest of the UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = { newText ->
                        // Update query, but do not trigger search yet
                        searchViewModel.onQueryChange(newText)
                    },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { searchViewModel.onStatusFilterChange(it) },
                    onSearchTriggered = {
                        // When icon pressed or Enter key pressed, run search
                        searchTriggered = true
                        searchViewModel.performSearch()
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        loading -> {
                            CircularProgressIndicator()
                        }
                        !searchTriggered || query.isBlank() -> {
                            // If no search triggered or query is blank, show the welcome text
                            Text("Discover A New Word Here")
                        }
                        query.isNotBlank() && results.isEmpty() -> {
                            // Search triggered, query not blank, but no results
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
    } else{

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Pane (35%)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .padding(8.dp)
            ) {
                // Box at top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(25.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search | 查询",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                    )
                }

                // SearchBar below the Box
                SearchBar(
                    query = query,
                    onQueryChange = {
                        searchViewModel.onQueryChange(it)
                        // No search triggered on query change alone
                    },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { newStatus ->
                        searchViewModel.onStatusFilterChange(newStatus)
                        // performSearch() in LaunchedEffect if conditions met
                    },
                    onSearchTriggered = {
                        searchTriggered = true
                        if (query.isNotBlank()) {
                            searchViewModel.performSearch()
                        }
                    }
                )
            }

            // Right Pane (65%) for results
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    loading -> {
                        CircularProgressIndicator()
                    }
                    !searchTriggered || query.isBlank() -> {
                        // If no search triggered or query is blank, show nothing
                        Text("Discover A New Word Here")
                    }
                    query.isNotBlank() && results.isEmpty() -> {
                        Text("No words match your search", style = MaterialTheme.typography.bodyLarge)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
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
    onStatusFilterChange: (String) -> Unit,
    onSearchTriggered: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            label = { Text("Search for a word (Chinese or English)") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        // Trigger search when icon is clicked
                        onSearchTriggered()
                    }
                )
            },
            singleLine = true,
            // When Enter key is pressed, perform search
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchTriggered()
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter (Dropdown or Row of Buttons)
        // For simplicity, let's do a row of RadioButtons:
        val options = listOf("all", "unseen", "learning", "mastered")
        if (!isLandscape()){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { option ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = (statusFilter == option),
                            onClick = { onStatusFilterChange(option) }
                        )
                        Text(text = option.capitalize())
                    }
                }
            }
        } else{
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // First two options in the first row
                    listOf(options[0], options[1]).forEach { option ->
                        Row(modifier = Modifier.weight(1f),verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (statusFilter == option),
                                onClick = { onStatusFilterChange(option) }
                            )
                            Text(text = option.capitalize())
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Last two options in the second row
                    listOf(options[2], options[3]).forEach { option ->
                        Row(modifier = Modifier.weight(1f),verticalAlignment = Alignment.CenterVertically) {
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
            Text(text = wordItem.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${wordItem.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
