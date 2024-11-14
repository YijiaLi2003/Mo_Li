package com.example.vocab_learning

import android.content.Context
import androidx.room.Room
import com.example.vocab_learning.data.VocabularyDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: VocabularyDatabase? = null

    fun getDatabase(context: Context): VocabularyDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                VocabularyDatabase::class.java,
                "vocabulary_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
