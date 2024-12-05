package com.example.vocab.api

import com.example.vocab.model.OxfordPronunciationResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OxfordDictionaryApi {

    @GET("entries/en/{word}")
    suspend fun getPronunciations(
        @Path("word") word: String
    ): OxfordPronunciationResponse
}
