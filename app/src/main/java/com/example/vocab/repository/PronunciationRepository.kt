//PronunciationRepository.kt
package com.example.vocab.repository


import com.example.vocab.api.ApiClient
import com.example.vocab.model.OxfordPronunciationResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch

class PronunciationRepository {

    private val api = ApiClient.oxfordApi

    fun getPronunciation(word: String): Flow<Result<String>> = flow {
        val response = api.getPronunciations(word)
        // Extract the audio file URL from the response
        val audioUrl = response.results
            .flatMap { it.lexicalEntries }
            .flatMap { it.entries }
            .flatMap { it.pronunciations ?: emptyList() }
            .firstOrNull { it.audioFile != null }
            ?.audioFile

        if (audioUrl != null) {
            emit(Result.success(audioUrl))
        } else {
            emit(Result.failure(Exception("Pronunciation not found")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}
