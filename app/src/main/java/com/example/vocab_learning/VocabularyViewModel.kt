package com.example.vocab_learning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VocabularyViewModel(context: Context) : ViewModel() {
    private val dao = DatabaseProvider.getDatabase(context).vocabularyDao()
    private val _topFiveWords = MutableStateFlow<List<VocabularyWord>>(emptyList())
    val topFiveWords: StateFlow<List<VocabularyWord>> = _topFiveWords

    init {
        viewModelScope.launch {
            _topFiveWords.value = dao.getTopFiveWords()
        }
    }
}

class VocabularyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
