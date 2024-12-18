// FavoriteWordsViewModel.kt
package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteWordsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _favoriteWords = MutableStateFlow<List<WordItem>>(emptyList())
    val favoriteWords: StateFlow<List<WordItem>> = _favoriteWords

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

        viewModelScope.launch {
            fetchFavoriteWords()
        }
    }

    private suspend fun fetchFavoriteWords() {
        _loading.value = true
        try {
            // Fetch favorite WordProgress entries
            val favoriteProgressList: List<WordProgress> = repository.getFavoriteWords(userId)

            // Map WordProgress to WordItem
            val wordItems = favoriteProgressList.mapNotNull { wp ->
                val vocab = repository.getVocabularyById(wp.wordId)
                vocab?.let {
                    WordItem(
                        wordId = wp.wordId,
                        word = it.word,
                        translation = it.translation,
                        status = wp.status,
                        isFavorite = wp.isFavorite
                    )
                }
            }

            _favoriteWords.value = wordItems
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally, handle errors (e.g., update UI with error state)
        } finally {
            _loading.value = false
        }
    }

    // Function to toggle favorite status
    fun toggleFavorite(wordId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(wordId, userId)
            fetchFavoriteWords() // Refresh the list after toggling
        }
    }

    // Optional: Method to refresh favorite words
    fun refreshFavoriteWords() {
        viewModelScope.launch {
            fetchFavoriteWords()
        }
    }
}
