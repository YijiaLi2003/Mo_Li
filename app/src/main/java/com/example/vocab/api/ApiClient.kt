package com.example.vocab.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private val client = OkHttpClient.Builder()
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/api/v2/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val dictionaryApi: DictionaryApi by lazy {
        retrofit.create(DictionaryApi::class.java)
    }
}
