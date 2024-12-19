// QuizViewModel.kt
package com.example.vocab.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.api.ApiClient
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.random.Random

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _words = MutableStateFlow<List<WordItem>>(emptyList())
    val words: StateFlow<List<WordItem>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    // Use this to let UI know when quiz is finished
    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished

    private val client = OkHttpClient()

    companion object {
        private const val MAX_CACHE_SIZE = 20
        private const val AUDIO_EXTENSION = ".mp3"
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "defaultUser"

        // Fetch words with status=learning
        viewModelScope.launch {
            val learningProgressList = repository.getWordsByStatus("learning", userId, Int.MAX_VALUE)

            val wordItems = mutableListOf<WordItem>()
            for (wp in learningProgressList) {
                val vocab = repository.getVocabularyById(wp.wordId)
                if (vocab != null) {
                    wordItems.add(
                        WordItem(
                            wordId = wp.wordId,
                            word = vocab.word,
                            translation = vocab.translation,
                            status = wp.status,
                            isFavorite = wp.isFavorite
                        )
                    )
                }
            }

            // Shuffle words
            val shuffled = wordItems.shuffled(Random(System.currentTimeMillis()))
            _words.value = shuffled

            // Cache pronunciation audio for each word with cache limit
            cacheAudioForWords(shuffled)
        }
    }

    private suspend fun cacheAudioForWords(wordItems: List<WordItem>) {
        withContext(Dispatchers.IO) {
            for (item in wordItems) {
                try {
                    val definitions = ApiClient.dictionaryApi.getWordDefinition(item.word)
                    // Try to find an audio link
                    val audioUrl = definitions
                        .flatMap { it.phonetics ?: emptyList() }
                        .firstOrNull { it.audio?.isNotEmpty() == true }
                        ?.audio

                    if (!audioUrl.isNullOrBlank()) {
                        // Download and save locally
                        downloadAndSaveAudio(item.word, audioUrl)
                    }
                } catch (e: Exception) {
                    Log.e("QuizAudioCache", "Error fetching audio for ${item.word}: ${e.message}")
                }
            }
        }
    }

    private fun downloadAndSaveAudio(word: String, url: String) {
        try {
            // Ensure the URL starts with "http" or "https"
            var finalUrl = url.trim()
            if (finalUrl.startsWith("//")) {
                finalUrl = "https:$finalUrl"
            } else if (!finalUrl.startsWith("http")) {
                finalUrl = "https://$finalUrl"
            }

            if (finalUrl.isBlank()) {
                Log.e("QuizAudioCache", "No valid audio URL for $word")
                return
            }

            val request = Request.Builder().url(finalUrl).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    val file = getAudioFileForWord(word)
                    file.writeBytes(audioBytes)
                    Log.d("QuizAudioCache", "Audio cached for $word at ${file.absolutePath}")

                    // After saving, enforce cache size limit
                    enforceCacheSizeLimit()
                } else {
                    Log.e("QuizAudioCache", "No audio data received for $word")
                }
            } else {
                Log.e("QuizAudioCache", "Failed to fetch audio for $word: HTTP ${response.code}")
            }
            response.close()
        } catch (e: Exception) {
            Log.e("QuizAudioCache", "Failed to cache audio for $word: ${e.message}")
        }
    }

    private fun getAudioFileForWord(word: String): File {
        val dir = getApplication<Application>().filesDir
        return File(dir, "$word$AUDIO_EXTENSION")
    }

    private fun enforceCacheSizeLimit() {
        val dir = getApplication<Application>().filesDir
        val audioFiles = dir.listFiles { file ->
            file.isFile && file.extension.equals("mp3", ignoreCase = true)
        }?.sortedBy { it.lastModified() } ?: return

        if (audioFiles.size > MAX_CACHE_SIZE) {
            val filesToDelete = audioFiles.take(audioFiles.size - MAX_CACHE_SIZE)
            for (file in filesToDelete) {
                if (file.delete()) {
                    Log.d("QuizAudioCache", "Deleted cached audio file: ${file.name}")
                } else {
                    Log.e("QuizAudioCache", "Failed to delete cached audio file: ${file.name}")
                }
            }
        }
    }

    fun onKnow() {
        viewModelScope.launch {
            val w = currentWord() ?: return@launch
            // Mark current word as mastered and correct
            val progress = repository.getWordProgress(w.wordId, userId)
            if (progress != null) {
                val updated = progress.copy(
                    status = "mastered",
                    isCorrect = true,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.updateWordProgress(updated)
                uploadWordProgressToFirebase(updated)
            }
            nextWord()
        }
    }

    fun onForget() {
        viewModelScope.launch {
            val w = currentWord() ?: return@launch
            // Mark current word as not correct and increment wrongCount
            val progress = repository.getWordProgress(w.wordId, userId)
            if (progress != null) {
                val updated = progress.copy(
                    isCorrect = false,
                    wrongCount = progress.wrongCount + 1,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.updateWordProgress(updated)
                uploadWordProgressToFirebase(updated)
            }
            nextWord()
        }
    }

    private suspend fun nextWord() {
        val current = _currentIndex.value
        val list = _words.value
        if (current < list.size - 1) {
            _currentIndex.value = current + 1
        } else {
            // End of quiz
            finishQuiz()
        }
    }

    private suspend fun finishQuiz() {
        // Set quizFinished to true
        _quizFinished.value = true
    }

    private fun currentWord(): WordItem? {
        val idx = _currentIndex.value
        val list = _words.value
        return if (idx in list.indices) list[idx] else null
    }

    private fun uploadWordProgressToFirebase(wordProgress: WordProgress) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("users")
            .document(wordProgress.userId)
            .collection("word_progress")
            .document(wordProgress.wordId.toString())

        docRef.set(wordProgress)
    }

    // Utility function to play audio
    fun playAudio(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = getAudioFileForWord(word)
            if (file.exists()) {
                // Trigger playback on the main thread
                withContext(Dispatchers.Main) {
                    try {
                        val mediaPlayer = android.media.MediaPlayer()
                        mediaPlayer.setDataSource(file.absolutePath)
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        mediaPlayer.setOnCompletionListener {
                            it.release()
                        }
                    } catch (e: Exception) {
                        Log.e("QuizAudioPlay", "Error playing audio for $word: ${e.message}")
                        // Optionally, notify the user about the error
                    }
                }
            } else {
                Log.e("QuizAudioPlay", "Audio file does not exist for $word")
                // Optionally, notify the user that audio is unavailable
            }
        }
    }

    fun toggleFavorite(wordId: Int) {
        viewModelScope.launch {
            val word = _words.value.find { it.wordId == wordId }
            if (word != null) {
                val newFavoriteStatus = !word.isFavorite
                val progress = repository.getWordProgress(wordId, userId)
                if (progress != null) {
                    val updated = progress.copy(
                        isFavorite = newFavoriteStatus,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateWordProgress(updated)
                    uploadWordProgressToFirebase(updated)

                    // Update the _words list
                    _words.value = _words.value.map {
                        if (it.wordId == wordId) it.copy(isFavorite = newFavoriteStatus) else it
                    }
                }
            }
        }
    }
}
