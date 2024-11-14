package com.example.vocab_learning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val type: String,
    val translation: String
)
