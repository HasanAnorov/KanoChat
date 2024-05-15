package com.ierusalem.androchat.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStorePreferenceRepository(context: Context) {

    private val Context.dataStore by preferencesDataStore(Constants.DATA_STORE_NAME)
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val defaultLanguage = Constants.DEFAULT_LOCALE
    private val defaultTheme = Constants.DEFAULT_THEME

    companion object {
        val PREF_LANGUAGE = stringPreferencesKey(name = Constants.PREFERENCE_LANGUAGE)
        val PREF_THEME = booleanPreferencesKey(name = Constants.PREFERENCE_THEME)

        private var INSTANCE: DataStorePreferenceRepository? = null

        @Suppress("unused")
        fun getInstance(context: Context): DataStorePreferenceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }
                val instance = DataStorePreferenceRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun setTheme(isSystemInDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PREF_THEME] = isSystemInDarkMode
        }
    }

    val getTheme: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PREF_THEME] ?: defaultTheme
        }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PREF_LANGUAGE] = language
        }
    }

    val getLanguage: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_LANGUAGE] ?: defaultLanguage
        }

}