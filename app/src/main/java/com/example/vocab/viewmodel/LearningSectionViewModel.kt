package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.UserProgress
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _desiredWordCount = MutableStateFlow<Int?>(null)
    val desiredWordCount: StateFlow<Int?> = _desiredWordCount

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "default_user"

        viewModelScope.launch {
            // Initially no words chosen
            _bookName.value = "Select the number of words to learn"
            _loading.value = false
        }
    }

    fun setDesiredWordCount(count: Int) {
        viewModelScope.launch {
            _loading.value = true
            _desiredWordCount.value = count

            val unseenProgressList = repository.getWordsByStatus("unseen", userId, count)
            println("Fetched unseen words: ${unseenProgressList.size}")

            if (unseenProgressList.isEmpty()) {
                _words.value = emptyList()
                recalculateProgress()
                _loading.value = false
                return@launch
            }

            val wordItems = mutableListOf<WordItem>()
            for (wp in unseenProgressList) {
                val vocab = repository.getVocabularyById(wp.wordId)
                if (vocab != null) {
                    wordItems.add(
                        WordItem(
                            wordId = wp.wordId,
                            word = vocab.word,
                            translation = vocab.translation,
                            status = wp.status // remains 'unseen'
                        )
                    )
                }
            }

            _words.value = wordItems
            // Set the "bookName" to reflect how many words user selected
            _bookName.value = "You selected $count words"
            recalculateProgress()
            _loading.value = false
            println("SetDesiredWordCount complete. Words: ${_words.value.size}, loading: false")
        }
    }

    private suspend fun recalculateProgress() {
        val allProgress = _words.value
        val count = _desiredWordCount.value

        // Count how many words are not unseen
        val unseenCount = allProgress.count { it.status == "unseen" }
        val totalChosen = allProgress.size
        val nonUnseenCount = totalChosen - unseenCount

        val progress = if (count != null && count > 0) {
            nonUnseenCount.toFloat() / count
        } else {
            0f
        }

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

    fun resetLearningSet() {
        viewModelScope.launch {
            _desiredWordCount.value = null
            _words.value = emptyList()
            _progressPercentage.value = 0f
            _bookName.value = "Select the number of words to learn"
            _loading.value = false
        }
    }

}
