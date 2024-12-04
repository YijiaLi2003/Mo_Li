//WordProgress.kt
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
    val id: Int = 0,
    val quizId: Int? = null,
    val userId: String = "default_user",  // Placeholder for user ID
    val wordId: Int,
    val isCorrect: Boolean = false,
    val status: String = "unseen",  // "unseen", "learning", "mastered"
    val quizAttempts: Int = 0,
    val wrongCount: Int = 0,
    val needsReview: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
