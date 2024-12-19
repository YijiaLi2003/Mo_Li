// NotificationReceiver.kt
package com.example.vocab.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.NotificationSettings
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("NotificationReceiver", "Alarm triggered")

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

            // Get a random wrong word
            val wrongWord = repository.getRandomWrongWord(userId)
            if (wrongWord != null) {
                val vocab = repository.getVocabularyById(wrongWord.wordId)
                if (vocab != null) {
                    val title = "Review '${vocab.word}'"
                    val message = "Meaning: ${vocab.translation}, wrong count: ${wrongWord.wrongCount}"
                    withContext(Dispatchers.Main) {
                        NotificationHelper.showNotification(context, title, message)
                    }
                } else {
                    Log.w("NotificationReceiver", "No vocab found for wrong word")
                }
            } else {
                Log.d("NotificationReceiver", "No wrong words found.")
            }
        }
    }
}
