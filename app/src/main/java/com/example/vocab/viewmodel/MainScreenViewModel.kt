//MainScreenViewModel.kt
package com.example.vocab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {

    private val _userName = MutableStateFlow("New User") // Replace!!!!!!
    val userName: StateFlow<String> = _userName

    private val _currentWordBook = MutableStateFlow("GRE Core 1800") // Replace!!!!!!
    val currentWordBook: StateFlow<String> = _currentWordBook

    private val _progress = MutableStateFlow(0.84f) // Replace!!!!!!
    val progress: StateFlow<Float> = _progress

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        // Simulate fetching data from backend
        viewModelScope.launch {
            // Replace with actual backend call!!!!!!!
            val currentUser = auth.currentUser
            _userName.value = currentUser?.email ?: "New User" //            _userName.value = "Yuanman Mu"

            _currentWordBook.value = "GRE Core 3000" // Replace with fetched word book name!!!!!!!!
            _progress.value = 0.84f // Replace with fetched progress value (56%)!!!!!!!
        }
    }
}