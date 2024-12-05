package com.example.vocab.model

import com.squareup.moshi.Json

data class OxfordPronunciationResponse(
    @Json(name = "results")
    val results: List<Result>
)

data class Result(
    @Json(name = "id")
    val id: String,
    @Json(name = "lexicalEntries")
    val lexicalEntries: List<LexicalEntry>
)

data class LexicalEntry(
    @Json(name = "entries")
    val entries: List<Entry>
)

data class Entry(
    @Json(name = "pronunciations")
    val pronunciations: List<Pronunciation>?
)

data class Pronunciation(
    @Json(name = "audioFile")
    val audioFile: String?,
    @Json(name = "phoneticSpelling")
    val phoneticSpelling: String?
)
