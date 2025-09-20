package com.ierusalem.androchat.core.app

import android.app.Application
import android.app.LocaleManager
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.directory_router.FilesDirectoryService
import com.ierusalem.androchat.core.emulator_detection.EmulatorDetector
import com.ierusalem.androchat.core.updater.UpdaterRepository
import com.ierusalem.androchat.core.updater.UpdaterWorker
import com.ierusalem.androchat.core.utils.log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class AndroChatApp : Application(), Configuration.Provider {

    @Inject
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    @Inject
    lateinit var updaterWorkerFactory: UpdaterWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(updaterWorkerFactory)
            .build()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()


        val isRunningOnEmulatorDetector = EmulatorDetector.isRunningOnEmulator()
        if (isRunningOnEmulatorDetector != null && isRunningOnEmulatorDetector) {
            log("Emulator detected!")
            throw IllegalStateException("Mobile Device Required!")
        }

        GlobalScope.launch(Dispatchers.IO) {
            dataStorePreferenceRepository.getUniqueDeviceId.collect { uniqueDeviceId ->
                if (uniqueDeviceId.isNotEmpty()) {
                    log("unique device id found: $uniqueDeviceId")
                } else {
                    log("unique device id not found")
                    val uniqueID = UUID.randomUUID().toString()
                    dataStorePreferenceRepository.setUniqueDeviceId(uniqueID)
                    log("unique device id generated: $uniqueID")
                }
            }
        }

        GlobalScope.launch {
            dataStorePreferenceRepository.getLanguage.collect { languageCode ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    applicationContext.getSystemService(LocaleManager::class.java).applicationLocales =
                        LocaleList.forLanguageTags(languageCode)
                } else {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(languageCode)
                    )
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            dataStorePreferenceRepository.getTheme.collect { isSystemInDarkMode ->
                if (isSystemInDarkMode) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        applicationContext.getSystemService(UiModeManager::class.java)
                            .setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        applicationContext.getSystemService(UiModeManager::class.java)
                            .setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
            }
        }

    }
}

class UpdaterWorkerFactory @Inject constructor(
    private val updaterRepository: UpdaterRepository,
    private val filesDirectoryService: FilesDirectoryService,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return UpdaterWorker(
            updaterRepository = updaterRepository,
            filesDirectoryService = filesDirectoryService,
            context = appContext,
            workerParameters = workerParameters
        )
    }
}