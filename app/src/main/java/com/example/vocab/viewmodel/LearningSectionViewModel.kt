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

data class WordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val status: String // "unseen", "learning", "mastered"
)

class LearningSectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _bookName = MutableStateFlow("Loading...")
    val bookName: StateFlow<String> = _bookName

    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage

    private val _words = MutableStateFlow<List<WordItem>>(emptyList())
    val words: StateFlow<List<WordItem>> = _words

    // Loading state
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

        viewModelScope.launch {
            _bookName.value = "TOEFL"

            val unseenProgressList = repository.getWordsByStatus("unseen", userId, Int.MAX_VALUE)
            for (wp in unseenProgressList) {
                val updated = wp.copy(status = "learning", lastUpdated = System.currentTimeMillis())
                repository.updateWordProgress(updated)
                uploadWordProgressToFirebase(updated)
            }

            val learningWordsProgress = repository.getWordsByStatus("learning", userId, Int.MAX_VALUE)
            val wordItems = mutableListOf<WordItem>()
            for (wp in learningWordsProgress) {
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
            recalculateProgress()

            // Data loaded
            _loading.value = false
        }
    }

    private suspend fun recalculateProgress() {
        val allProgress = repository.getAllWordProgress(userId)
        val totalWords = allProgress.size
        val masteredCount = allProgress.count { it.status == "mastered" }
        val progress = if (totalWords > 0) masteredCount.toFloat() / totalWords else 0f
        _progressPercentage.value = progress
    }

    suspend fun updateWordStatus(wordId: Int, newStatus: String) {
        val currentProgress = repository.getWordProgress(wordId, userId)
        if (currentProgress != null) {
            val updated = currentProgress.copy(
                status = newStatus,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateWordProgress(updated)
            uploadWordProgressToFirebase(updated)

            val updatedList = _words.value.map { item ->
                if (item.wordId == wordId) item.copy(status = newStatus) else item
            }
            _words.value = updatedList
            recalculateProgress()
        }
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