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

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _userName = MutableStateFlow("New User")
    val userName: StateFlow<String> = _userName

    private val _currentWordBook = MutableStateFlow("Loading...")
    val currentWordBook: StateFlow<String> = _currentWordBook

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = auth.currentUser?.uid
        userId = uid ?: "defaultUser"

        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            _userName.value = currentUser?.email ?: "New User"

            _currentWordBook.value = "TOEFL"

            val allProgress = repository.getAllWordProgress(userId)
            val totalWords = allProgress.size
            val masteredCount = allProgress.count { it.status == "mastered" }

            val progressValue = if (totalWords > 0) masteredCount.toFloat() / totalWords else 0f
            _progress.value = progressValue
        }
    }
}
