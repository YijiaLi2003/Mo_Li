//vocabulary_dao.kt
package com.example.vocab.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vocab.model.Vocabulary

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary")
    suspend fun getAllVocabulary(): List<Vocabulary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: Vocabulary)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVocabulary(vocabularyList: List<Vocabulary>)

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun getCount(): Int

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getVocabularyById(id: Int): Vocabulary?
}
