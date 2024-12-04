package com.example.vocab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.QuizRecord
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import kotlinx.coroutines.launch

class VocabularyViewModel(application: Application, private val userId: String) : AndroidViewModel(application) {

    private val repository: VocabularyRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(
            database.vocabularyDao(),
            database.wordProgressDao(),
            database.quizRecordDao()
        )
    }

    fun getWordProgress(wordId: Int, onResult: (WordProgress?) -> Unit) {
        viewModelScope.launch {
            val progress = repository.getWordProgress(wordId, userId)
            onResult(progress)
        }
    }

    fun updateWordProgress(wordId: Int, isCorrect: Boolean) {
        viewModelScope.launch {
            val existingProgress = repository.getWordProgress(wordId, userId)
            val newProgress = existingProgress?.copy(
                isCorrect = isCorrect,
                quizAttempts = existingProgress.quizAttempts + 1,
                lastUpdated = System.currentTimeMillis()
            ) ?: WordProgress(
                wordId = wordId,
                userId = userId,
                isCorrect = isCorrect,
                quizAttempts = 1
            )
            repository.insertWordProgress(newProgress)
        }
    }

    fun recordQuiz(totalQuestions: Int, correctAnswers: Int, wrongAnswers: Int) {
        viewModelScope.launch {
            val quizRecord = QuizRecord(
                userId = userId,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                wrongAnswers = wrongAnswers
            )
            repository.insertQuizRecord(quizRecord)
        }
    }

}
