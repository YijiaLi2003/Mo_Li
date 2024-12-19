// FreedictionaryModels.kt
package com.example.vocab.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FreedictionaryResponseItem(
    val word: String,
    val phonetic: String?,
    val phonetics: List<FreedictionaryPhonetic>?,
    val origin: String?,
    val meanings: List<FreedictionaryMeaning>
)

@JsonClass(generateAdapter = true)
data class FreedictionaryPhonetic(
    val text: String?,
    val audio: String?
    // You can include other fields if needed, e.g., sourceUrl, license
)

@JsonClass(generateAdapter = true)
data class FreedictionaryMeaning(
    val partOfSpeech: String,
    val definitions: List<FreedictionaryDefinition>
)

@JsonClass(generateAdapter = true)
data class FreedictionaryDefinition(
    val definition: String,
    val example: String?,
    val synonyms: List<String>,
    val antonyms: List<String>
)
