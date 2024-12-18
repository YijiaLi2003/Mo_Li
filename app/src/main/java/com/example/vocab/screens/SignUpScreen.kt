package com.example.vocab.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vocab.model.WordProgress
import com.example.vocab.ui.theme.Screen
import com.example.vocab.viewmodel.VocabularyViewModel
import com.example.vocab.viewmodel.VocabularyViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val vocabularyViewModel: VocabularyViewModel = viewModel(
        factory = VocabularyViewModelFactory(application)
    )
    val coroutineScope = rememberCoroutineScope()

    // Email validation regex
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            // Show a loading indicator
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {


                Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!email.matches(emailRegex)) {
                            errorMessage = "Invalid email format."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password must not be empty."
                            return@Button
                        }
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val currentUser = auth.currentUser
                                    val userId = currentUser?.uid
                                    if (userId != null) {
                                        coroutineScope.launch {
                                            val result = initializeWordProgressLocally(userId, vocabularyViewModel)
                                            isLoading = false
                                            result.fold(
                                                onSuccess = {
                                                    // Navigate to Main Screen
                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(Screen.SignUp.route) { inclusive = true }
                                                    }
                                                },
                                                onFailure = { error ->
                                                    errorMessage = "Failed to initialize data: ${error.message}"
                                                }
                                            )
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Failed to retrieve user ID."
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = task.exception?.message ?: "Registration failed."
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate(Screen.SignIn.route) }) {
                    Text("Already have an account? Sign In")
                }
            }
        }
    }
}


suspend fun initializeWordProgressLocally(
    userId: String,
    vocabularyViewModel: VocabularyViewModel
): Result<Unit> {
    return try {
        val vocabularyList = vocabularyViewModel.getAllVocabulary()
        if (vocabularyList.isNotEmpty()) {
            val wordProgressList = vocabularyList.map { vocab ->
                WordProgress(
                    userId = userId,
                    wordId = vocab.id,
                    status = "unseen"
                )
            }
            vocabularyViewModel.insertWordProgressList(wordProgressList)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Vocabulary list is empty."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
