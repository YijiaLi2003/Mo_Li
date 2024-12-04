package com.example.vocab.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.vocab.model.QuizRecord

@Dao
interface QuizRecordDao {

    @Insert
    suspend fun insertQuizRecord(quizRecord: QuizRecord)

    @Query("SELECT * FROM quiz_records WHERE userId = :userId ORDER BY dateTaken DESC")
    suspend fun getQuizRecords(userId: String): List<QuizRecord>

    @Query("SELECT * FROM quiz_records WHERE quizId = :quizId")
    suspend fun getQuizRecordById(quizId: Int): QuizRecord?

    @Query("SELECT * FROM quiz_records")
    suspend fun getAllQuizRecords(): List<QuizRecord>
}
