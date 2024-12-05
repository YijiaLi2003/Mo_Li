package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReStudyWordsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _words = MutableStateFlow<List<WordItem>>(emptyList())
    val words: StateFlow<List<WordItem>> = _words

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

        viewModelScope.launch {
            // Fetch words that are still "learning"
            val learningProgressList = repository.getWordsByStatus("learning", userId, Int.MAX_VALUE)

            val wordItems = mutableListOf<WordItem>()
            for (wp in learningProgressList) {
                val vocab = repository.getVocabularyById(wp.wordId)
                if (vocab != null) {
                    wordItems.add(
                        WordItem(
                            wordId = wp.wordId,
                            word = vocab.word,
                            translation = vocab.translation,
                            status = wp.status
                        )
                    )
                }
            }

            _words.value = wordItems
            _loading.value = false
        }
    }
}
