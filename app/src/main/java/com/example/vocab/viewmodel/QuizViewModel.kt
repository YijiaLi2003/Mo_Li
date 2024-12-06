package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _words = MutableStateFlow<List<WordItem>>(emptyList())
    val words: StateFlow<List<WordItem>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

        // Fetch words with status=learning
        viewModelScope.launch {
            val learningProgressList = repository.getWordsByStatus("learning", userId, Int.MAX_VALUE)

            // Join with vocabulary
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

            // Shuffle words
            val shuffled = wordItems.shuffled(Random(System.currentTimeMillis()))
            _words.value = shuffled
        }
    }

    fun onKnow() {
        viewModelScope.launch {
            val w = currentWord() ?: return@launch
            // Mark current word as mastered
            val progress = repository.getWordProgress(w.wordId, userId)
            if (progress != null) {
                val updated = progress.copy(
                    status = "mastered",
                    lastUpdated = System.currentTimeMillis()
                )
                repository.updateWordProgress(updated)
                uploadWordProgressToFirebase(updated)
            }
            nextWord()
        }
    }

    fun onForget() {
        viewModelScope.launch {
            // Do not change status, just move to the next word
            nextWord()
        }
    }

    private suspend fun nextWord() {
        val current = _currentIndex.value
        val list = _words.value
        if (current < list.size - 1) {
            _currentIndex.value = current + 1
        } else {
            // End of quiz
            // TODO: Show a message or handle quiz completion
        }
    }

    private fun currentWord(): WordItem? {
        val idx = _currentIndex.value
        val list = _words.value
        return if (idx in list.indices) list[idx] else null
    }

    private fun uploadWordProgressToFirebase(wordProgress: WordProgress) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("users")
            .document(wordProgress.userId)
            .collection("word_progress")
            .document(wordProgress.wordId.toString())

        docRef.set(wordProgress)
    }
}
