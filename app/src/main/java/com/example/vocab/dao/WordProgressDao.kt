// WordProgressDao.kt
package com.example.vocab.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vocab.model.WordProgress

@Dao
interface WordProgressDao {
    @Query("SELECT * FROM word_progress WHERE wordId = :wordId AND userId = :userId")
    suspend fun getWordProgress(wordId: Int, userId: String): WordProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordProgress(wordProgress: WordProgress)

    @Update
    suspend fun updateWordProgress(wordProgress: WordProgress)

    @Query("SELECT * FROM word_progress WHERE id = :id")
    suspend fun getWordProgressById(id: Int): WordProgress?

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getWordProgressByWordId(wordId: Int): WordProgress?

    @Query("SELECT * FROM word_progress WHERE status = :status AND userId = :userId LIMIT :limit")
    suspend fun getWordsByStatus(status: String, userId: String, limit: Int): List<WordProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wordProgressList: List<WordProgress>)

    @Query("SELECT * FROM word_progress WHERE userId = :userId")
    suspend fun getAllWordProgress(userId: String): List<WordProgress>

    // Existing query to fetch favorite words
    @Query("SELECT * FROM word_progress WHERE isFavorite = 1 AND userId = :userId")
    suspend fun getFavoriteWords(userId: String): List<WordProgress>


    @Query("SELECT * FROM word_progress WHERE isCorrect = 0 AND userId = :userId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWrongWord(userId: String): WordProgress?

}
