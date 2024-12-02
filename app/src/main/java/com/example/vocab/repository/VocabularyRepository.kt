package com.example.vocab.repository

import com.example.vocab.dao.VocabularyDao
import com.example.vocab.dao.WordProgressDao
import com.example.vocab.dao.QuizRecordDao
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress

class VocabularyRepository(
    private val vocabularyDao: VocabularyDao,
    private val wordProgressDao: WordProgressDao,
    private val quizRecordDao: QuizRecordDao
) {

    suspend fun getAllVocabulary(): List<Vocabulary> {
        return vocabularyDao.getAllVocabulary()
    }

    suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocabularyDao.getVocabularyById(id)
    }

    suspend fun getWordProgressByWordId(wordId: Int): WordProgress? {
        return wordProgressDao.getWordProgressByWordId(wordId)
    }

    suspend fun insertWordProgress(wordProgress: WordProgress) {
        wordProgressDao.insertWordProgress(wordProgress)
    }

    suspend fun updateWordProgress(wordProgress: WordProgress) {
        wordProgressDao.updateWordProgress(wordProgress)
    }

    suspend fun getWordsByStatus(status: String, limit: Int): List<WordProgress> {
        return wordProgressDao.getWordsByStatus(status, limit)
    }

}
