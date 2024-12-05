package com.example.vocab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LearningSectionViewModel : ViewModel() {
    private val _bookName = MutableStateFlow("Loading...")
    val bookName: StateFlow<String> = _bookName

    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage

    init {
        viewModelScope.launch {
            // Simulate network delay
            delay(1000L)
            // TODO: Fetch data from backend and update _bookName and _progressPercentage
            _bookName.value = "TOEFL" // Example fetched data
            _progressPercentage.value = 0.84f // Example fetched data
        }
    }
}