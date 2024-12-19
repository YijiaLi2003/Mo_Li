package com.example.vocab.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vocab.database.AppDatabase
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("NotificationReceiver", "Alarm triggered, attempting to show a wrong word notification")

        // We use a CoroutineScope to do database operations
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

            // Get words that the user got wrong
            val wrongWord = repository.getRandomWrongWord(userId)
            if (wrongWord != null) {

                // Fetch the vocabulary details
                val vocab = repository.getVocabularyById(wrongWord.wordId)

                if (vocab != null) {
                    val word = vocab.word
                    val translation = vocab.translation
                    val wrongCount = wrongWord.wrongCount

                    val title = "Memory Refresh: $word"
                    val message = "You got '$word' wrong $wrongCount times. Meaning: $translation"

                    withContext(Dispatchers.Main) {
                        NotificationHelper.showNotification(context, title, message)
                    }
                } else {
                    // If vocab is null, skip notification
                    Log.w("NotificationReceiver", "No vocabulary found for the chosen wrong word.")
                }
            } else {
                Log.d("NotificationReceiver", "No wrong words found for user $userId")
            }
        }
    }
}
