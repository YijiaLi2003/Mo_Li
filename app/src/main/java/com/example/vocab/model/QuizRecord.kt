//QuizRecord.kt
package com.example.vocab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_records")
data class QuizRecord(
    @PrimaryKey(autoGenerate = true)
    val quizId: Int = 0,
    val userId: String,
    val dateTaken: Long = System.currentTimeMillis(),
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int
)

