package com.example.vocab_learning.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vocab_learning.VocabularyWord

@Database(entities = [VocabularyWord::class], version = 1)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao
}
