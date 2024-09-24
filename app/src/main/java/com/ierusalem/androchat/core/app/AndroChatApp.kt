package com.ierusalem.androchat.core.app

import android.app.Application
import android.app.LocaleManager
import android.app.UiModeManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.emulator_detection.EmulatorDetector
import com.ierusalem.androchat.core.utils.log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class AndroChatApp : Application() {

    @Inject
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val isRunningOnEmulatorDetector = EmulatorDetector.isRunningOnEmulator()
        if (isRunningOnEmulatorDetector != null && isRunningOnEmulatorDetector) {
            log("Emulator detected!")
            throw IllegalStateException("Mobile Device Required!")
        }

        GlobalScope.launch(Dispatchers.IO) {
            dataStorePreferenceRepository.getUniqueDeviceId.collect{ uniqueDeviceId ->
                if (uniqueDeviceId.isNotEmpty()){
                    log("unique device id found: $uniqueDeviceId")
                }else{
                    log("unique device id not found")
                    val uniqueID = UUID.randomUUID().toString()
                    dataStorePreferenceRepository.setUniqueDeviceId(uniqueID)
                    log("unique device id generated: $uniqueID")
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            dataStorePreferenceRepository.getSessionId.collect{ sessionId ->
                if (sessionId.isNotEmpty()){
                    log("session id found: $sessionId")
                }else{
                    log("session id not found")
                    val uniqueID = UUID.randomUUID().toString()
                    dataStorePreferenceRepository.setSessionId(uniqueID)
                    log("session id generated: $uniqueID")
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