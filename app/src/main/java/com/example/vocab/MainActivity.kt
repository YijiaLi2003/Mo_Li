package com.example.vocab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.vocab.database.AppDatabase
import com.example.vocab.dao.VocabularyDao
import com.example.vocab.model.Vocabulary
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private lateinit var vocabularyDao: VocabularyDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DAOs
        val database = AppDatabase.getDatabase(this)
        vocabularyDao = database.vocabularyDao()

        // Import the vocabulary data
        lifecycleScope.launch {
            importVocabularyFromCsv()
        }

        setContent {
            //Compose UI goes here
        }
    }

    private suspend fun importVocabularyFromCsv() {
        withContext(Dispatchers.IO) {
            val existingCount = vocabularyDao.getCount()
            if (existingCount > 0) {
                // When Data already imported
                return@withContext
            }

            try {
                val inputStream = assets.open("vocabulary.csv")
                val reader = CSVReaderBuilder(InputStreamReader(inputStream, "UTF-8"))
                    .withCSVParser(
                        CSVParserBuilder()
                            .withSeparator(',')
                            .withQuoteChar('"')
                            .withEscapeChar('\\')
                            .build()
                    )
                    .build()

                var nextLine: Array<String>?
                val vocabularyList = mutableListOf<Vocabulary>()
                while (true) {
                    nextLine = reader.readNext()
                    if (nextLine == null) {
                        break
                    }
                    if (nextLine.isEmpty()) continue

                    // The first field is the word
                    val word = nextLine[0].trim()
                    // The second field is the translation, possibly containing newlines
                    val translation = nextLine.getOrNull(1)?.trim() ?: ""

                    val vocabulary = Vocabulary(
                        word = word,
                        translation = translation
                    )
                    vocabularyList.add(vocabulary)
                }

                reader.close()

                // Insert all vocabulary into database
                vocabularyDao.insertAllVocabulary(vocabularyList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
