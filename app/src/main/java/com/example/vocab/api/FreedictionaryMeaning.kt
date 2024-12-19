package com.example.vocab.api

import retrofit2.http.GET
import retrofit2.http.Path

interface FreedictionaryApi {
    @GET("entries/en/{word}")
    suspend fun getWordDefinition(@Path("word") word: String): List<FreedictionaryResponseItem>
}
