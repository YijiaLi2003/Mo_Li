package com.example.vocab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val translation: String,
    val type: String? = null,
    val definition: String? = null,
    val exampleSentence: String? = null,
    val pronunciationUrl: String? = null
)
