//Vocabulary Repository
package com.example.vocab.repository

import androidx.lifecycle.LiveData
import com.example.vocab.dao.VocabularyDao
import com.example.vocab.dao.WordProgressDao
import com.example.vocab.dao.QuizRecordDao
import com.example.vocab.model.QuizRecord
import com.example.vocab.model.Vocabulary
import com.example.vocab.model.WordProgress
import com.example.vocab.viewmodel.WordItem

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

    suspend fun insertWordProgressList(wordProgressList: List<WordProgress>) {
        wordProgressDao.insertAll(wordProgressList)
    }

    suspend fun getAllWordProgress(userId: String): List<WordProgress> {
        return wordProgressDao.getAllWordProgress(userId)
    }

    suspend fun searchWords(queryValue: String, statusValue: String?, userId: String): List<WordItem> {
        val vocabList = vocabularyDao.searchVocabulary(queryValue)
        val wordItems = mutableListOf<WordItem>()

        for (vocab in vocabList) {
            val wp = wordProgressDao.getWordProgress(vocab.id, userId)
            if (wp != null) {
                if (statusValue == null || statusValue == "all" || wp.status == statusValue) {
                    wordItems.add(
                        WordItem(
                            wordId = vocab.id,
                            word = vocab.word,
                            translation = vocab.translation,
                            status = wp.status
                        )
                    )
                }
            }
        }

        return wordItems
    }



}
