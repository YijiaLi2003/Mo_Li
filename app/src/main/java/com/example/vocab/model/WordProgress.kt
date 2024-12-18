package com.example.vocab.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_progress",
    foreignKeys = [
        ForeignKey(
            entity = Vocabulary::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"])]
)
data class WordProgress(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var quizId: Int? = null,
    var userId: String = "",
    var wordId: Int = 0,
    var isCorrect: Boolean = false,
    var status: String = "unseen",  // "unseen", "learning", "mastered"
    var quizAttempts: Int = 0,
    var wrongCount: Int = 0,
    var needsReview: Boolean = false,
    var isFavorite: Boolean = false, // New field
    var lastUpdated: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase serialization
    constructor() : this(0, null, "", 0)
}
