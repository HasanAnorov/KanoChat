package com.ierusalem.androchat.core.utils

import android.os.Build
import com.ierusalem.androchat.core.app.AppLanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Constants {

    const val DATA_STORE_NAME = "AppDataStore"

    const val FILE_RECEIVE_TIMEOUT = 10 * 1000
    const val INFINITELY_TIMEOUT = 0

    const val PREFERENCE_LANGUAGE = "device_language"
    const val PREFERENCE_BROADCAST_FREQUENCY = "device_broadcast_frequency"
    const val PREFERENCE_THEME = "device_theme"
    const val PREFERENCE_LOGGING_STATUS = "device_logging_status"
    const val PREFERENCE_USERNAME = "device_user_username"
    const val PREFERENCE_HOTSPOT_NAME = "device_hotspot_name"
    const val PREFERENCE_HOTSPOT_PASSWORD = "device_hotspot_password"
    const val PREFERENCE_PORT_NUMBER = "device_port_number"
    const val PREFERENCE_UNIQUE_DEVICE_ID = "device_unique_id"

    const val SELECTED_CHATTING_USER = "selected_chatting_user"

    const val UNKNOWN_HOTSPOT_NAME = "TEMP-HOTSPOT"

    const val MINIMUM_LOGIN_LENGTH = 3

    const val MAX_PORT_NUMBER = 65000
    const val MIN_PORT_NUMBER = 1025

    const val MAX_HOTSPOT_NAME_LENGTH = 32
    const val MIN_HOTSPOT_NAME_LENGTH = 5

    const val MAX_HOTSPOT_PASSWORD_LENGTH = 16
    const val MIN_HOTSPOT_PASSWORD_LENGTH = 8

    const val DEFAULT_PORT_NUMBER = "9002"
    const val DEFAULT_HOTSPOT_NAME = "FAST-CHAT"

    private const val ENGLISH_LOCALE = "en"
    private const val RUSSIAN_LOCALE = "ru"

    const val DEFAULT_THEME = false
    const val DEFAULT_BROADCAST_FREQUENCY = "FREQUENCY_2_4_GHZ"
    const val DEFAULT_LOCALE = RUSSIAN_LOCALE

    const val CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    const val FOLDER_NAME_FOR_RESOURCES = "FastChat"
    const val FILE_PROVIDER_AUTHORITY = "com.ierusalem.androchat.fileprovider"

    const val SOCKET_DEFAULT_BUFFER_SIZE = 4 * 1024

    const val MESSAGES_DATABASE_NAME = "messages_db"

    const val VOICE_MESSAGE_FILE_NAME = "audio_"
    const val AUDIO_EXTENSION = ".mp3"

    fun getRandomColor(): Int {
        return RandomColors().getColor()
    }

    fun getLanguageCode(language: AppLanguage): String {
        return when (language) {
            AppLanguage.English -> ENGLISH_LOCALE
            AppLanguage.Russian -> RUSSIAN_LOCALE
        }
    }

    fun getLanguageFromCode(languageCode: String): AppLanguage {
        return when (languageCode) {
            ENGLISH_LOCALE -> AppLanguage.English
            RUSSIAN_LOCALE -> AppLanguage.Russian
            else -> AppLanguage.Russian
        }
    }

    fun generateUniqueFileName(directory: String, baseFileName: String, extension: String): String {
        var count = 0
        var newFileName: String
        val baseNameWithExtension = "$baseFileName.$extension"

        do {
            count++
            newFileName = if (count == 1) {
                "$directory/$baseNameWithExtension"
            } else {
                "$directory/$baseFileName($count).$extension"
            }
        } while (File(newFileName).exists())

        return newFileName
    }

    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        val currentDate = calendar.get(Calendar.DATE)

        val currentYear = calendar.get(Calendar.YEAR)

        val monthDate = SimpleDateFormat("MMM", Locale.getDefault())
        val currentShortMontName = monthDate.format(calendar.time)
        val currentTime =
            "$currentHour:$currentMinute:$currentSecond, $currentShortMontName $currentDate $currentYear"

        return currentTime
    }

    fun getTimeInHours(): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val time = ("${currentHour}_${currentMinute}_$currentSecond")
        return time
    }

    fun getSimpleDate(): String {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.get(Calendar.DATE)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val date = "_${currentDate}_${currentMonth}_${currentYear}"
        return date
    }

    fun isValidVersionForLocalOnlyHotspot(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

}