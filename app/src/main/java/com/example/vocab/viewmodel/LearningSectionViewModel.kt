package com.example.vocab.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocab.api.ApiClient
import com.example.vocab.api.FreedictionaryResponseItem
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class WordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val status: String, // "unseen", "learning", "mastered"
    val isFavorite: Boolean = false
)

data class DetailedWordInfo(
    val phonetic: String?,
    val examples: List<String>,
    val translation: String
)

class LearningSectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository
    private val userId: String

    private val _bookName = MutableStateFlow("TOEFL")
    val bookName: StateFlow<String> = _bookName

    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage

    private val _words = MutableStateFlow<List<WordItem>>(emptyList())
    val words: StateFlow<List<WordItem>> = _words

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _desiredWordCount = MutableStateFlow<Int?>(null)
    val desiredWordCount: StateFlow<Int?> = _desiredWordCount

    private val _favoriteWords = MutableStateFlow<List<WordItem>>(emptyList())
    private val _detailedInfo = MutableStateFlow<DetailedWordInfo?>(null)
    val detailedInfo: StateFlow<DetailedWordInfo?> = _detailedInfo

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userId = uid ?: "default_user"

        viewModelScope.launch {
            _loading.value = false
        }
    }

    fun setDesiredWordCount(count: Int) {
        viewModelScope.launch {
            _loading.value = true
            _desiredWordCount.value = count

            val unseenProgressList = repository.getWordsByStatus("unseen", userId, count)
            Log.d("LearningSectionViewModel", "Fetched unseen words: ${unseenProgressList.size}")

            if (unseenProgressList.isEmpty()) {
                _words.value = emptyList()
                recalculateProgress()
                _loading.value = false
                return@launch
            }

            val wordItems = mutableListOf<WordItem>()
            for (wp in unseenProgressList) {
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

            _words.value = wordItems

            withContext(Dispatchers.IO) {
                cacheAudioForWords(wordItems)
                cacheDefinitionsForWords(wordItems)
            }

            recalculateProgress()
            _loading.value = false
        }
    }

    private suspend fun cacheAudioForWords(wordItems: List<WordItem>) {
        withContext(Dispatchers.IO) {
            for (item in wordItems) {
                try {
                    val definitions = loadDefinitionData(item.word)
                    val audioUrl = definitions
                        .flatMap { it.phonetics ?: emptyList() }
                        .firstOrNull { it.audio?.isNotEmpty() == true }
                        ?.audio

                    if (!audioUrl.isNullOrBlank()) {
                        downloadAndSaveAudio(item.word, audioUrl)
                    }
                } catch (e: Exception) {
                    Log.e("AudioCache", "Error fetching audio for ${item.word}: ${e.message}")
                }
            }
        }
    }

    private suspend fun cacheDefinitionsForWords(wordItems: List<WordItem>) {
        withContext(Dispatchers.IO) {
            for (item in wordItems) {
                try {
                    loadDefinitionData(item.word) // Will cache if not cached
                } catch (e: Exception) {
                    Log.e("DefinitionCache", "Error caching definition for ${item.word}: ${e.message}")
                }
            }
        }
    }

    private fun downloadAndSaveAudio(word: String, url: String) {
        try {
            var finalUrl = url.trim()
            if (finalUrl.startsWith("//")) {
                finalUrl = "https:$finalUrl"
            } else if (!finalUrl.startsWith("http")) {
                finalUrl = "https://$finalUrl"
            }

            if (finalUrl.isBlank()) {
                Log.e("AudioCache", "No valid audio URL for $word")
                return
            }

            val client = OkHttpClient()
            val request = Request.Builder().url(finalUrl).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    val file = getAudioFileForWord(word)
                    file.writeBytes(audioBytes)
                    Log.d("AudioCache", "Audio cached for $word at ${file.absolutePath}")
                } else {
                    Log.e("AudioCache", "No audio data received for $word")
                }
            } else {
                Log.e("AudioCache", "Failed to fetch audio for $word: HTTP ${response.code}")
            }
            response.close()
        } catch (e: Exception) {
            Log.e("AudioCache", "Failed to cache audio for $word: ${e.message}")
        }
    }

    private fun getAudioFileForWord(word: String): File {
        val dir = getApplication<Application>().filesDir
        return File(dir, "$word.mp3")
    }

    private fun getDefinitionCacheFile(word: String): File {
        val dir = getApplication<Application>().filesDir
        return File(dir, "$word-definition.json")
    }

    private suspend fun loadDefinitionData(word: String): List<FreedictionaryResponseItem> {
        return withContext(Dispatchers.IO) {
            val file = getDefinitionCacheFile(word)
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<List<FreedictionaryResponseItem>>(
                Types.newParameterizedType(List::class.java, FreedictionaryResponseItem::class.java)
            )

            if (file.exists()) {
                val json = file.readText()
                Log.d("LearningSectionViewModel", "Loading cached definition for $word")
                val data = adapter.fromJson(json)
                if (!data.isNullOrEmpty()) {
                    Log.d("LearningSectionViewModel", "Loaded cached definition for $word")
                    return@withContext data
                } else {
                    Log.e("LearningSectionViewModel", "Cached definition is empty for $word")
                }
            }

            try {
                Log.d("LearningSectionViewModel", "Fetching definition from API for $word")
                val fetched = ApiClient.dictionaryApi.getWordDefinition(word)
                if (fetched.isNotEmpty()) {
                    val json = adapter.toJson(fetched)
                    file.writeText(json)
                    Log.d("LearningSectionViewModel", "Cached definition for $word")
                } else {
                    Log.e("LearningSectionViewModel", "No definition fetched for $word")
                }
                return@withContext fetched
            } catch (e: Exception) {
                Log.e("LearningSectionViewModel", "API call failed for $word: ${e.message}")
                return@withContext emptyList<FreedictionaryResponseItem>()
            }
        }
    }

    suspend fun loadDetailedInfo(wordId: Int) {
        val wItem = _words.value.find { it.wordId == wordId } ?: return
        val word = wItem.word
        _detailedInfo.value = null

        val definitions = try {
            loadDefinitionData(word)
        } catch (e: Exception) {
            Log.e("LearningSectionViewModel", "Error loading definitions for $word: ${e.message}")
            emptyList()
        }

        val firstItem = definitions.firstOrNull()
        val phonetic: String? = firstItem?.phonetic ?: firstItem?.phonetics?.firstOrNull()?.text
        val exampleList = mutableListOf<String>()
        firstItem?.meanings?.forEach { meaning ->
            meaning.definitions.forEach { def ->
                def.example?.let { exampleList.add(it) }
            }
        }

        val translation = wItem.translation

        // Log the fetched data
        Log.d("LearningSectionViewModel", "Word: $word")
        Log.d("LearningSectionViewModel", "Phonetic: $phonetic")
        Log.d("LearningSectionViewModel", "Examples: $exampleList")

        // If we got no data at all (firstItem == null), then just show translation.
        // Otherwise, show what we have, even if phonetic or examples are empty.
        val info = if (firstItem == null) {
            // No data from API, fallback
            DetailedWordInfo(
                phonetic = null,
                examples = emptyList(),
                translation = translation
            )
        } else {
            // We have some data, show phonetic/examples if available
            DetailedWordInfo(
                phonetic = phonetic,
                examples = exampleList,
                translation = translation
            )
        }

        _detailedInfo.value = info
    }

    private fun recalculateProgress() {
        val allProgress = _words.value
        val count = _desiredWordCount.value

        val unseenCount = allProgress.count { it.status == "unseen" }
        val totalChosen = allProgress.size
        val nonUnseenCount = totalChosen - unseenCount

        val progress = if (count != null && count > 0) {
            nonUnseenCount.toFloat() / count
        } else {
            0f
        }

        _progressPercentage.value = progress
    }

    suspend fun updateWordStatus(wordId: Int, newStatus: String) {
        val currentProgress = repository.getWordProgress(wordId, userId)
        if (currentProgress != null) {
            val updated = currentProgress.copy(
                status = newStatus,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateWordProgress(updated)
            uploadWordProgressToFirebase(updated)

            val updatedList = _words.value.map { item ->
                if (item.wordId == wordId) item.copy(status = newStatus) else item
            }
            _words.value = updatedList
            recalculateProgress()
        }
    }

    private fun uploadWordProgressToFirebase(wordProgress: WordProgress) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("users")
            .document(wordProgress.userId)
            .collection("word_progress")
            .document(wordProgress.wordId.toString())

        docRef.set(wordProgress)
    }

    fun resetLearningSet() {
        viewModelScope.launch {
            _desiredWordCount.value = null
            _words.value = emptyList()
            _progressPercentage.value = 0f
            _bookName.value = "TOEFL"
            _loading.value = false
        }
    }

    private fun loadFavoriteWords() {
        viewModelScope.launch {
            val favoriteWordProgressList = repository.getFavoriteWords(userId)
            val favoriteWordItems = favoriteWordProgressList.map { wp ->
                val vocab = repository.getVocabularyById(wp.wordId)
                WordItem(
                    wordId = wp.wordId,
                    word = vocab?.word ?: "",
                    translation = vocab?.translation ?: "",
                    status = wp.status,
                    isFavorite = wp.isFavorite
                )
            }
            _favoriteWords.value = favoriteWordItems
        }
    }

    fun toggleFavorite(wordId: Int) {
        viewModelScope.launch {
            val wordProgress = repository.getWordProgress(wordId, userId)
            if (wordProgress != null) {
                val newFavoriteStatus = !wordProgress.isFavorite
                val updatedWordProgress = wordProgress.copy(isFavorite = newFavoriteStatus)
                repository.updateWordProgress(updatedWordProgress)
                uploadWordProgressToFirebase(updatedWordProgress)
                _words.value = _words.value.map { item ->
                    if (item.wordId == wordId) item.copy(isFavorite = newFavoriteStatus) else item
                }
                loadFavoriteWords()
            }
        }
    }
}
