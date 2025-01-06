package com.merahputihperkasa.prodigi.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import kotlinx.coroutines.flow.first


class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i("Prodigi.DataSync", "DataSyncWorker started")
        val context = applicationContext
        val repository = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

        return try {
            repository.getBannerItems(forceRefresh = true).first()
            repository.getFilteredContents("", forceRefresh = true).first()
            Log.i("Prodigi.DataSync", "DataSyncWorker finished")

            Result.success()
        } catch (e: Exception) {
            Log.e("Prodigi.DataSync", "DataSyncWorker failed: ${e.message}")
            Result.retry()
        }
    }
}
