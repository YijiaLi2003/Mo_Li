// SignInScreen.kt
package com.example.vocab.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun SignInScreen(navController: NavController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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


                Text("Sign In", style = MaterialTheme.typography.headlineMedium)
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

                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Validate inputs
                        when {
                            email.isBlank() || password.isBlank() -> {
                                errorMessage = "Email and password must not be empty."
                            }
                            !email.matches(emailRegex) -> {
                                errorMessage = "Invalid email format."
                            }
                            else -> {
                                isLoading = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val currentUser = auth.currentUser
                                            val userId = currentUser?.uid
                                            if (userId != null) {
                                                coroutineScope.launch {
                                                    val result = downloadWordProgressFromFirebase(
                                                        userId,
                                                        vocabularyViewModel
                                                    )
                                                    isLoading = false
                                                    result.fold(
                                                        onSuccess = {
                                                            // Navigate to Main Screen
                                                            navController.navigate(Screen.Home.route) {
                                                                popUpTo(Screen.SignIn.route) { inclusive = true }
                                                            }
                                                        },
                                                        onFailure = { error ->
                                                            errorMessage =
                                                                "Failed to download data: ${error.message}"
                                                        }
                                                    )
                                                }
                                            } else {
                                                isLoading = false
                                                errorMessage = "Failed to retrieve user ID."
                                            }
                                        } else {
                                            isLoading = false
                                            errorMessage = task.exception?.message ?: "Authentication failed."
                                        }
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
                    Text("Don't have an account? Sign Up")
                }
            }
        }
    }
}

suspend fun downloadWordProgressFromFirebase(
    userId: String,
    vocabularyViewModel: VocabularyViewModel
): Result<Unit> {
    return try {
        val firestore = FirebaseFirestore.getInstance()
        val wordProgressCollection = firestore.collection("users")
            .document(userId)
            .collection("word_progress")

        // Fetch remote word progress data
        val snapshot = withContext(Dispatchers.IO) {
            wordProgressCollection.get().await()
        }
        val remoteWordProgressList = snapshot.documents.mapNotNull { document ->
            document.toObject(WordProgress::class.java)
        }

        // Fetch local word progress data
        val localWordProgressList = vocabularyViewModel.getAllWordProgress(userId)

        // Merge remote and local data
        val mergedList = mergeWordProgressData(localWordProgressList, remoteWordProgressList)

        // Update local database with merged data
        vocabularyViewModel.insertWordProgressList(mergedList)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun mergeWordProgressData(
    localList: List<WordProgress>,
    remoteList: List<WordProgress>
): List<WordProgress> {
    val mergedMap = localList.associateBy { it.wordId }.toMutableMap()

    for (remoteProgress in remoteList) {
        val localProgress = mergedMap[remoteProgress.wordId]
        if (localProgress == null || remoteProgress.lastUpdated > localProgress.lastUpdated) {
            mergedMap[remoteProgress.wordId] = remoteProgress
        }
    }

    return mergedMap.values.toList()
}
