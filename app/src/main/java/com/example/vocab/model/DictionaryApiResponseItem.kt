package com.example.vocab.model

import com.squareup.moshi.Json

data class DictionaryApiResponseItem(
    @Json(name = "word") val word: String,
    @Json(name = "phonetic") val phonetic: String?,
    @Json(name = "phonetics") val phonetics: List<Phonetic>?,
    @Json(name = "origin") val origin: String?,
    @Json(name = "meanings") val meanings: List<Meaning>
)

data class Phonetic(
    @Json(name = "text") val text: String?,
    @Json(name = "audio") val audio: String?
)

data class Meaning(
    @Json(name = "partOfSpeech") val partOfSpeech: String,
    @Json(name = "definitions") val definitions: List<Definition>
)

data class Definition(
    @Json(name = "definition") val definition: String,
    @Json(name = "example") val example: String?,
    @Json(name = "synonyms") val synonyms: List<String>,
    @Json(name = "antonyms") val antonyms: List<String>
)
