package com.ierusalem.androchat.core.updater

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ierusalem.androchat.core.utils.log
import kotlinx.coroutines.delay

class UpdaterWorker(
    context: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        log("work is in progress ...")
        delay(2000)
        log("work is finished")
        return Result.success()
    }

}