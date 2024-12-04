package com.example.vocab.repository

import com.example.vocab.dao.VocabularyDao
import com.example.vocab.dao.WordProgressDao
import com.example.vocab.dao.QuizRecordDao
import com.example.vocab.model.QuizRecord
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress

class VocabularyRepository(
    private val vocabularyDao: VocabularyDao,
    private val wordProgressDao: WordProgressDao,
    private val quizRecordDao: QuizRecordDao
) {
    suspend fun getWordProgress(wordId: Int, userId: String): WordProgress? {
        return wordProgressDao.getWordProgress(wordId, userId)
    }

    suspend fun getAllVocabulary(): List<Vocabulary> {
        return vocabularyDao.getAllVocabulary()
    }

    suspend fun insertQuizRecord(quizRecord: QuizRecord) {
        quizRecordDao.insertQuizRecord(quizRecord)
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

    suspend fun getWordsByStatus(status: String, userId: String, limit: Int): List<WordProgress> {
        return wordProgressDao.getWordsByStatus(status, userId, limit)
    }

    suspend fun getQuizRecords(userId: String): List<QuizRecord> {
        return quizRecordDao.getQuizRecords(userId)
    }

}
