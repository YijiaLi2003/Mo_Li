package com.example.vocab.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.vocab.dao.VocabularyDao
import com.example.vocab.dao.WordProgressDao
import com.example.vocab.dao.QuizRecordDao
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress
import com.example.vocab.model.QuizRecord

@Database(entities = [Vocabulary::class, WordProgress::class, QuizRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun quizRecordDao(): QuizRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocab_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
