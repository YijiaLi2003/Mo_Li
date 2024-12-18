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

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _statusFilter = MutableStateFlow("all") // "all", "unseen", "learning", "mastered"
    val statusFilter: StateFlow<String> = _statusFilter

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _results = MutableStateFlow<List<WordItem>>(emptyList())
    val results: StateFlow<List<WordItem>> = _results

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onStatusFilterChange(newStatus: String) {
        _statusFilter.value = newStatus
    }

    fun performSearch() {
        viewModelScope.launch {
            _loading.value = true
            val queryValue = _query.value
            val statusValue = if (_statusFilter.value == "all") null else _statusFilter.value

            val resultList = repository.searchWords(queryValue, statusValue, userId)
            _results.value = resultList
            _loading.value = false
        }
    }
}
