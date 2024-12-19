// DataSyncWorker.kt
package com.example.vocab.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vocab.database.AppDatabase
import com.example.vocab.model.WordProgress
import com.example.vocab.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DataSyncWorker"
    }

    // Initialize repository
    private val repository: VocabularyRepository

    init {
        val db = AppDatabase.getDatabase(appContext)
        repository = VocabularyRepository(db.vocabularyDao(), db.wordProgressDao(), db.quizRecordDao())
    }

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Sync aborted.")
            return Result.failure()
        }

        val userId = currentUser.uid

        return try {
            // Fetch remote word progress data
            val remoteSnapshot = withContext(Dispatchers.IO) {
                firestore.collection("users")
                    .document(userId)
                    .collection("word_progress")
                    .get()
                    .await()
            }

            val remoteWordProgressList = remoteSnapshot.documents.mapNotNull { document ->
                document.toObject(WordProgress::class.java)
            }

            // Fetch local word progress data
            val localWordProgressList = repository.getAllWordProgress(userId)

            // Merge remote and local data
            val mergedList = mergeWordProgressData(localWordProgressList, remoteWordProgressList)

            // Update local database with merged data
            repository.insertWordProgressList(mergedList)

            // Optionally, upload local changes to Firestore
            // Implement if needed (e.g., push local updates to remote)

            Log.d(TAG, "Data synchronization successful.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Data synchronization failed: ${e.message}")
            Result.retry() // Retry the work in case of failure
        }
    }

    private fun mergeWordProgressData(
        localList: List<WordProgress>,
        remoteList: List<WordProgress>
    ): List<WordProgress> {
        val mergedMap = localList.associateBy { it.wordId }.toMutableMap()

        for (remoteProgress in remoteList) {
            val localProgress = mergedMap[remoteProgress.wordId]
            if (localProgress == null || remoteProgress.lastUpdated > localProgress.lastUpdated) {
                mergedMap[remoteProgress.wordId] = remoteProgress
            }
        }

        return mergedMap.values.toList()
    }
}
