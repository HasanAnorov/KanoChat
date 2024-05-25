package com.ierusalem.androchat.utils

import com.ierusalem.androchat.app.AppLanguage

object Constants {

    const val DATA_STORE_NAME = "AppDataStore"
    const val PREFERENCE_LANGUAGE = "device_language"
    const val PREFERENCE_THEME = "device_theme"

    const val USERNAME_REGISTER_TO_HOME = "username_register_to_home"
    const val MINIMUM_LOGIN_LENGTH = 3
    const val MAX_BADGE_COUNT = 99

    const val MAX_PORT_NUMBER = 65000
    const val MIN_PORT_NUMBER = 1025

    private const val ENGLISH_LOCALE = "en"
    private const val RUSSIAN_LOCALE = "ru"

    const val DEFAULT_THEME = false
    const val DEFAULT_LOCALE = RUSSIAN_LOCALE

    const val CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

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

}