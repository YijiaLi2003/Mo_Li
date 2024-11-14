package com.example.vocab_learning

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

suspend fun parseCSV(context: Context): List<VocabularyWord> {
    val wordList = mutableListOf<VocabularyWord>()
    val inputStream = context.assets.open("vocabulary.csv")
    val reader = BufferedReader(withContext(Dispatchers.IO) {
        InputStreamReader(inputStream, "UTF-8")
    })

    reader.useLines { lines ->
        lines.forEach { line ->
            val parsedWord = parseLine(line)
            if (parsedWord != null) {
                wordList.add(parsedWord)
            }
        }
    }

    return wordList
}

fun parseLine(line: String): VocabularyWord? {
    if (line.trim().isEmpty()) return null

    val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
    if (parts.size >= 2) {
        val word = parts[0].trim()
        val rest = parts.drop(1).joinToString(",").trim().removeSurrounding("\"")
        val typeAndTranslation = rest.split(".", limit = 2)
        if (typeAndTranslation.size == 2) {
            val type = typeAndTranslation[0].trim()
            val translation = typeAndTranslation[1].trim()
            return VocabularyWord(word = word, type = type, translation = translation)
        } else if (typeAndTranslation.size == 1) {
            return VocabularyWord(word = word, type = "", translation = typeAndTranslation[0].trim())
        }
    }
    return null
}
