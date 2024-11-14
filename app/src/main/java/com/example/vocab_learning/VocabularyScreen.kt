package com.example.vocab_learning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun VocabularyScreen(viewModel: VocabularyViewModel) {
    val wordsState = viewModel.topFiveWords.collectAsState()

    LazyColumn {
        items(wordsState.value) { word ->
            VocabularyItem(word)
            HorizontalDivider()
        }
    }
}

@Composable
fun VocabularyItem(word: VocabularyWord) {
    Column {
        Text(
            text = word.word,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "${word.type}: ${word.translation}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
