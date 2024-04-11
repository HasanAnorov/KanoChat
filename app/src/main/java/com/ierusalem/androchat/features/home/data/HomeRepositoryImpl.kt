package com.ierusalem.androchat.features.home.data

import com.ierusalem.androchat.app.AppTheme
import com.ierusalem.androchat.features.home.domain.HomeRepository
import com.ierusalem.androchat.utils.PreferenceHelper

class HomeRepositoryImpl(private val preferenceHelper: PreferenceHelper): HomeRepository {
    override fun saveAppTheme(appTheme: AppTheme) {
        preferenceHelper.saveAppTheme(appTheme)
    }

    override fun getAppTheme(): AppTheme {
        val appThemeInString = preferenceHelper.getAppTheme()
        val appTheme = when(appThemeInString){
            AppTheme.Default.name -> AppTheme.Default
            AppTheme.Dark.name -> AppTheme.Dark
            AppTheme.Light.name -> AppTheme.Light
            else -> AppTheme.Default
        }
        return appTheme
    }
}