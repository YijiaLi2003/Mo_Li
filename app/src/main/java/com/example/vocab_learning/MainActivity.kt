package com.example.vocab_learning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: VocabularyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        populateDatabase()

        viewModel = ViewModelProvider(
            this,
            VocabularyViewModelFactory(this)
        )[VocabularyViewModel::class.java]

        setContent {
            VocabularyScreen(viewModel)
        }
    }

    private fun populateDatabase() {
        val dao = DatabaseProvider.getDatabase(this).vocabularyDao()

        CoroutineScope(Dispatchers.IO).launch {
            val words = parseCSV(this@MainActivity)
            dao.insertAll(words)
        }
    }
}
