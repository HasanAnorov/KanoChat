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
    private val defaultHotspotName = Constants.DEFAULT_HOTSPOT_NAME
    private val defaultUniqueDeviceId = ""
    private val defaultSessionId = ""

    companion object {
        val PREF_LANGUAGE = stringPreferencesKey(name = Constants.PREFERENCE_LANGUAGE)
        val PREF_BROADCAST_FREQUENCY =
            stringPreferencesKey(name = Constants.PREFERENCE_BROADCAST_FREQUENCY)
        val PREF_THEME = booleanPreferencesKey(name = Constants.PREFERENCE_THEME)
        val PREF_LOGGING_STATUS = booleanPreferencesKey(name = Constants.PREFERENCE_LOGGING_STATUS)
        val PREF_USERNAME = stringPreferencesKey(name = Constants.PREFERENCE_USERNAME)
        val PREF_HOTSPOT_NAME = stringPreferencesKey(name = Constants.PREFERENCE_HOTSPOT_NAME)
        val PREF_PORT_NUMBER = stringPreferencesKey(name = Constants.PREFERENCE_PORT_NUMBER)
        val PREF_HOTSPOT_PASSWORD =
            stringPreferencesKey(name = Constants.PREFERENCE_HOTSPOT_PASSWORD)

        val PREF_UNIQUE_DEVICE_ID =
            stringPreferencesKey(name = Constants.PREFERENCE_UNIQUE_DEVICE_ID)

        val PREF_SESSION_ID =
            stringPreferencesKey(name = Constants.PREFERENCE_SESSION_ID)


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

    suspend fun setLoggingStatus(isLogged: Boolean) {
        dataStore.edit { preferences ->
            preferences[PREF_LOGGING_STATUS] = isLogged
        }
    }

    private val getLoggingStatus: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PREF_LOGGING_STATUS] ?: false
        }

    suspend fun hasUserLoggedIn(): Boolean {
        return getUsername.first().isNotEmpty() && getLoggingStatus.first()
    }

    suspend fun setUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[PREF_USERNAME] = username
        }
    }

    val getUsername: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_USERNAME] ?: ""
        }

    suspend fun setSessionId(sessionId: String) {
        dataStore.edit { preferences ->
            preferences[PREF_SESSION_ID] = sessionId
        }
    }

    val getSessionId: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_SESSION_ID] ?: defaultSessionId
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

    suspend fun setPortNumber(portNumber: String) {
        dataStore.edit { preferences ->
            preferences[PREF_PORT_NUMBER] = portNumber
        }
    }

    val getHotspotPassword: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_HOTSPOT_PASSWORD] ?: generateRandomPassword(8)
        }

    val getPortNumber: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PREF_PORT_NUMBER] ?: Constants.DEFAULT_PORT_NUMBER
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