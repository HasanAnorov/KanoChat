package com.ierusalem.androchat.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ierusalem.androchat.app.AppTheme
class PreferenceHelper(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)

    fun saveAppTheme(appTheme: AppTheme) {
        with(sharedPref.edit()) {
            Log.d("ahi3646", "saveAppTheme: ${appTheme.name} ")
            putString(Constants.APP_THEME_KEY, appTheme.name)
            apply()
        }
    }

    fun getAppTheme(): String {
        return sharedPref.getString(Constants.APP_THEME_KEY, AppTheme.Default.name) ?: AppTheme.Default.name
    }


    fun saveToken(token: String) {
        with(sharedPref.edit()) {
            putString(Constants.TOKEN_KEY, "Bearer $token")
            apply()
        }
    }

    fun getToken(): String = sharedPref.getString(Constants.TOKEN_KEY, "")!!

    fun deleteToken() {
        with(sharedPref.edit()) {
            remove(Constants.TOKEN_KEY)
            apply()
        }
    }

    fun saveRefreshToken(token: String) {
        with(sharedPref.edit()) {
            putString(Constants.REFRESH_TOKEN_KEY, token)
            apply()
        }
    }

    fun getRefreshToken(): String = sharedPref.getString(Constants.REFRESH_TOKEN_KEY, "")!!

    fun deleteRefreshToken() {
        with(sharedPref.edit()) {
            remove(Constants.REFRESH_TOKEN_KEY)
            apply()
        }
    }
}

