package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.repository.VocabularyRepository
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(
            database.vocabularyDao(),
            database.wordProgressDao(),
            database.quizRecordDao()
        )
    }

    fun getWordsToLearn(count: Int, onResult: (List<Vocabulary>) -> Unit) {
        viewModelScope.launch {
            val words = withContext(Dispatchers.IO) {
                val wordProgressList = repository.getWordsByStatus("unseen", count)
                val wordIds = wordProgressList.map { it.wordId }

                // Update status to 'learning' in WordProgress
                wordProgressList.forEach {
                    val updatedProgress = it.copy(status = "learning")
                    repository.updateWordProgress(updatedProgress)
                }

                // Fetch Vocabulary entries
                wordIds.mapNotNull { repository.getVocabularyById(it) }
            }
            onResult(words)
        }
    }

    fun updateWordProgress(wordId: Int, knowsWord: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val wordProgress = repository.getWordProgressByWordId(wordId)
            if (wordProgress != null) {
                val updatedProgress = wordProgress.copy(
                    isCorrect = knowsWord,
                    status = if (knowsWord) "mastered" else "learning",
                    quizAttempts = wordProgress.quizAttempts + 1,
                    wrongCount = if (knowsWord) wordProgress.wrongCount else wordProgress.wrongCount + 1,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.updateWordProgress(updatedProgress)
            } else {
                val newProgress = WordProgress(
                    wordId = wordId,
                    isCorrect = knowsWord,
                    status = if (knowsWord) "mastered" else "learning",
                    quizAttempts = 1,
                    wrongCount = if (knowsWord) 0 else 1,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.insertWordProgress(newProgress)
            }
        }
    }


}
