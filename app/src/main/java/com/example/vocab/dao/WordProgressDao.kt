package com.example.vocab.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vocab.model.WordProgress

@Dao
interface WordProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordProgress(wordProgress: WordProgress)

    @Update
    suspend fun updateWordProgress(wordProgress: WordProgress)

    @Query("SELECT * FROM word_progress WHERE id = :id")
    suspend fun getWordProgressById(id: Int): WordProgress?

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getWordProgressByWordId(wordId: Int): WordProgress?

    @Query("SELECT * FROM word_progress WHERE status = :status LIMIT :limit")
    suspend fun getWordsByStatus(status: String, limit: Int): List<WordProgress>
}
