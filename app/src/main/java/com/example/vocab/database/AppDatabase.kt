package com.example.vocab.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vocab.dao.*
import com.example.vocab.model.*

@Database(
    entities = [Vocabulary::class, WordProgress::class, QuizRecord::class, UserProgress::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun quizRecordDao(): QuizRecordDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocab_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
