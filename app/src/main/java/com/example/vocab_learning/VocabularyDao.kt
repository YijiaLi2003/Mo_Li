package com.example.vocab_learning.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vocab_learning.VocabularyWord

@Dao
interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyWord>)

    @Query("SELECT * FROM vocabulary LIMIT 5")
    suspend fun getTopFiveWords(): List<VocabularyWord>
}
