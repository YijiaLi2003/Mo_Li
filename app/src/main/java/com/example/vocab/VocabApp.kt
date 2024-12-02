package com.example.vocab

import android.app.Application
import com.example.vocab.database.AppDatabase

class VocabApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
    }
}
