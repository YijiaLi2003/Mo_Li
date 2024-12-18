package com.example.vocab.api

import com.example.vocab.model.DictionaryApiResponseItem
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("entries/en/{word}")
    suspend fun getWordDefinition(
        @Path("word") word: String
    ): List<DictionaryApiResponseItem>
}
