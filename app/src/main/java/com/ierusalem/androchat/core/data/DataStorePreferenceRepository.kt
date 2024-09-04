package com.ierusalem.androchat.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ierusalem.androchat.core.app.AppBroadcastFrequency
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.generateRandomPassword
import com.ierusalem.androchat.core.utils.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStorePreferenceRepository(context: Context) {

    private val Context.dataStore by preferencesDataStore(Constants.DATA_STORE_NAME)
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val defaultLanguage = Constants.DEFAULT_LOCALE
    private val defaultTheme = Constants.DEFAULT_THEME
    private val defaultBroadcastFrequency = Constants.DEFAULT_BROADCAST_FREQUENCY
    private val defaultUsername = Constants.UNKNOWN_USER
    private val defaultPassword = Constants.DEFAULT_PASSWORD
    private val defaultHotspotName = Constants.DEFAULT_HOTSPOT_NAME
    private val defaultUniqueDeviceId = ""

    companion object {
        val PREF_LANGUAGE = stringPreferencesKey(name = Constants.PREFERENCE_LANGUAGE)
        val PREF_BROADCAST_FREQUENCY =
            stringPreferencesKey(name = Constants.PREFERENCE_BROADCAST_FREQUENCY)
        val PREF_THEME = booleanPreferencesKey(name = Constants.PREFERENCE_THEME)
        val PREF_USERNAME = stringPreferencesKey(name = Constants.PREFERENCE_USERNAME)
        val PREF_PASSWORD = stringPreferencesKey(name = Constants.PREFERENCE_PASSWORD)
        val PREF_HOTSPOT_NAME = stringPreferencesKey(name = Constants.PREFERENCE_HOTSPOT_NAME)
        val PREF_HOTSPOT_PASSWORD =
            stringPreferencesKey(name = Constants.PREFERENCE_HOTSPOT_PASSWORD)

        val PREF_UNIQUE_DEVICE_ID =
            stringPreferencesKey(name = Constants.PREFERENCE_UNIQUE_DEVICE_ID)

        private var INSTANCE: DataStorePreferenceRepository? = null

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

    suspend fun hasUserLoggedIn(): Boolean {
        return getUsername.first().isNotEmpty() && getPassword.first().isNotEmpty()
    }

    suspend fun setPassword(password: String) {
        dataStore.edit { preferences ->
            preferences[PREF_PASSWORD] = password
        }
    }

    private val getPassword: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_PASSWORD] ?: defaultPassword
        }

    suspend fun setUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[PREF_USERNAME] = username
        }
    }

    val getUsername: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_USERNAME] ?: defaultUsername
        }

    suspend fun setUniqueDeviceId(uniqueDeviceId: String) {
        dataStore.edit { preferences ->
            preferences[PREF_UNIQUE_DEVICE_ID] = uniqueDeviceId
        }
    }

    val getUniqueDeviceId: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_UNIQUE_DEVICE_ID] ?: defaultUniqueDeviceId
        }

    suspend fun setHotSpotName(hotspotName: String) {
        dataStore.edit { preferences ->
            preferences[PREF_HOTSPOT_NAME] = hotspotName
        }
    }

    val getHotspotName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_HOTSPOT_NAME] ?: defaultHotspotName
        }

    suspend fun setHotSpotPassword(hotspotPassword: String) {
        dataStore.edit { preferences ->
            preferences[PREF_HOTSPOT_PASSWORD] = hotspotPassword
        }
    }

    val getHotspotPassword: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_HOTSPOT_PASSWORD] ?: generateRandomPassword(8)
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

    suspend fun setBroadcastFrequency(frequency: AppBroadcastFrequency) {
        log("broadcast frequency - ${frequency.name}")
        dataStore.edit { preferences ->
            preferences[PREF_BROADCAST_FREQUENCY] = frequency.name
        }
    }

    val getBroadcastFrequency: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_BROADCAST_FREQUENCY] ?: defaultBroadcastFrequency
        }

}