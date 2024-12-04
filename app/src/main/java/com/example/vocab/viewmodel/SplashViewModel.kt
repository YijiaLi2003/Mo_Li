package com.example.vocab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    // Private mutable state flow
    private val _isSplashVisible = MutableStateFlow(true)

    // Public immutable state flow
    val isSplashVisible: StateFlow<Boolean> = _isSplashVisible

    init {
        // Launch a coroutine to hide the splash screen after a delay
        viewModelScope.launch {
            delay(3000L) // 3-second delay
            _isSplashVisible.value = false
        }
    }
}