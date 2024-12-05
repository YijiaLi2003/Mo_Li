package com.example.vocab.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val userId: String,
    val currentIndex: Int
)
