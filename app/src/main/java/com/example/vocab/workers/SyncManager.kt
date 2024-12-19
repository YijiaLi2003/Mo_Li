// SyncManager.kt
package com.example.vocab.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncManager {

    private const val UNIQUE_WORK_NAME = "DataSyncWork"

    fun schedulePeriodicSync(context: Context) {
        // Define constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Ensure network connectivity
            .setRequiresBatteryNotLow(true) // Avoid running when battery is low
            .build()

        // Define the periodic work request
        val syncWorkRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            1, // Repeat interval
            TimeUnit.HOURS // Repeat interval unit
        )
            .setConstraints(constraints)
            .build()

        // Enqueue the work uniquely to prevent duplicate workers
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
            syncWorkRequest
        )
    }

    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val immediateSyncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(immediateSyncRequest)
    }

    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
