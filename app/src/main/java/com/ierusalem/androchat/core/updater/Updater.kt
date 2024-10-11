package com.ierusalem.androchat.core.updater

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdaterWorker @AssistedInject constructor(
    @Assisted private val updaterRepository: UpdaterRepository,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        Log.d("worker", "work is in progress ...")
        if(updaterRepository.getUnUpdatedMessagesCount()>0){

            return Result.success()
        }else{
            Log.d("worker", "work is finished, no messages to upload!")
            return Result.success()
        }
    }

}