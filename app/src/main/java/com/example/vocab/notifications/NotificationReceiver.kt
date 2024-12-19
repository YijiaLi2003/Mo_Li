/**
 * NotificationReceiver.kt
 *
 * This class handles the broadcast alarm trigger for notifications.
 * It retrieves a random word marked as "wrong" for the current user and
 * displays a notification with the word and its details.
 */

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

/**
 * NotificationReceiver
 *
 * A broadcast receiver triggered by an alarm to display notifications.
 * It fetches a random "wrong" word for the current user and displays it as a notification.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Called when the receiver is triggered by the broadcast intent.
     *
     * @param context The application context.
     * @param intent The broadcast intent (can contain additional data, though unused here).
     */
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("NotificationReceiver", "Alarm triggered")

        // Start a coroutine to perform database operations asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val repository = VocabularyRepository(
                vocabularyDao = db.vocabularyDao(),
                wordProgressDao = db.wordProgressDao(),
                quizRecordDao = db.quizRecordDao()
            )
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

            // Fetch a random wrong word for the user
            val wrongWord = repository.getRandomWrongWord(userId)
            if (wrongWord != null) {
                val vocab = repository.getVocabularyById(wrongWord.wordId)
                if (vocab != null) {
                    // Create a notification with the word details
                    val title = vocab.word
                    val message = "Meaning: ${vocab.translation}, wrong count: ${wrongWord.wrongCount}"
                    withContext(Dispatchers.Main) {
                        NotificationHelper.showNotification(context, title, message)
                    }
                } else {
                    Log.w("NotificationReceiver", "No vocabulary found for the wrong word")
                }
            } else {
                Log.d("NotificationReceiver", "No wrong words found.")
            }
        }
    }
}
