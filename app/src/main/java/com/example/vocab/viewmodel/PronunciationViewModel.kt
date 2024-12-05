package com.example.vocab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.repository.PronunciationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PronunciationViewModel : ViewModel() {

    private val repository = PronunciationRepository()

    private val _audioUrl = MutableStateFlow<Result<String>?>(null)
    val audioUrl = _audioUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchPronunciation(word: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPronunciation(word).collect { result ->
                _audioUrl.value = result
                _isLoading.value = false
            }
        }
    }


    fun reset() {
        _audioUrl.value = null
    }
}
