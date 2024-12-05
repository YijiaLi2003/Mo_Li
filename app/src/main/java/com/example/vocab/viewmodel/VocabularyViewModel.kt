package com.example.vocab.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.QuizRecord
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(
            database.vocabularyDao(),
            database.wordProgressDao(),
            database.quizRecordDao()
        )
    }

    fun getWordProgress(wordId: Int, userId: String, onResult: (WordProgress?) -> Unit) {
        viewModelScope.launch {
            val progress = repository.getWordProgress(wordId, userId)
            onResult(progress)
        }
    }

    fun updateWordProgress(wordId: Int, userId: String, isCorrect: Boolean) {
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

    fun recordQuiz(userId: String, totalQuestions: Int, correctAnswers: Int, wrongAnswers: Int) {
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

    suspend fun getAllVocabulary(): List<Vocabulary> {
        return repository.getAllVocabulary()
    }

    suspend fun insertWordProgressList(wordProgressList: List<WordProgress>) {
        repository.insertWordProgressList(wordProgressList)
    }

    fun updateWordProgress(wordProgress: WordProgress) {
        viewModelScope.launch {
            repository.updateWordProgress(wordProgress)
            // Upload to Firebase
            uploadWordProgressToFirebase(wordProgress)
        }
    }

    private fun uploadWordProgressToFirebase(wordProgress: WordProgress) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = wordProgress.userId
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("word_progress")
            .document(wordProgress.wordId.toString())

        docRef.set(wordProgress)
            .addOnSuccessListener {
                // Handle success
                // Optionally log success or update UI
                Log.d("UploadWordProgress", "Successfully uploaded wordProgress for wordId: ${wordProgress.wordId}")
            }
            .addOnFailureListener { e ->
                // Handle failure
                // Log the error
                Log.e("UploadWordProgress", "Failed to upload wordProgress for wordId: ${wordProgress.wordId}", e)

                // Retry mechanism (optional)
                // You can implement a retry strategy or queue the failed upload for later
                handleUploadFailure(wordProgress, e)
            }
    }


    suspend fun getAllWordProgress(userId: String): List<WordProgress> {
        return repository.getAllWordProgress(userId)
    }

    private fun handleUploadFailure(wordProgress: WordProgress, exception: Exception) {
        // Log the exception details
        Log.e("UploadWordProgress", "Error uploading wordProgress: ${exception.localizedMessage}", exception)

        // Check the type of exception to determine if a retry is appropriate
        if (isNetworkError(exception)) {
            // Optionally implement a retry mechanism
            // For example, you can retry after a delay or enqueue the task
            retryUpload(wordProgress)
        } else {
            // Handle other types of errors appropriately
            // You might notify the user or log the error for further analysis
        }
    }

    // Helper method to determine if the exception was due to network issues
    private fun isNetworkError(exception: Exception): Boolean {
        // Check for common network exceptions
        return exception is FirebaseNetworkException ||
                exception is UnknownHostException ||
                exception is SocketTimeoutException
    }

    // Retry upload after a delay
    private fun retryUpload(wordProgress: WordProgress) {
        // Define the maximum number of retries
        val maxRetries = 3
        var retryCount = 0

        // Use a coroutine to handle the retry with delay
        viewModelScope.launch {
            while (retryCount < maxRetries) {
                delay(2000) // Wait for 2 seconds before retrying
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val userId = wordProgress.userId
                    val docRef = firestore.collection("users")
                        .document(userId)
                        .collection("word_progress")
                        .document(wordProgress.wordId.toString())

                    withContext(Dispatchers.IO) {
                        docRef.set(wordProgress).await()
                    }
                    // If upload is successful, exit the loop
                    Log.d("UploadWordProgress", "Retry successful for wordId: ${wordProgress.wordId}")
                    break
                } catch (e: Exception) {
                    retryCount++
                    Log.e("UploadWordProgress", "Retry $retryCount failed for wordId: ${wordProgress.wordId}", e)
                    if (retryCount >= maxRetries) {
                        Log.e("UploadWordProgress", "Max retries reached for wordId: ${wordProgress.wordId}")
                        // Optionally notify the user or log the failure
                    }
                }
            }
        }
    }




}
