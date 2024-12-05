//OxfordDictionaryApi.kt
package com.example.vocab.api

import com.example.vocab.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    private const val BASE_URL = "https://od-api.oxforddictionaries.com/api/v2/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Set to Level.NONE in production
    }

    // Interceptor to add headers
    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("app_id", BuildConfig.OXFORD_APP_ID)
            .addHeader("app_key", BuildConfig.OXFORD_APP_KEY)
            .build()
        chain.proceed(request)
    }

    // Build OkHttpClient
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(headerInterceptor)
        .build()

    // Build Retrofit instance
    val oxfordApi: OxfordDictionaryApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(OxfordDictionaryApi::class.java)
}
