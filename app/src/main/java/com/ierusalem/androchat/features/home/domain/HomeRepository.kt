package com.ierusalem.androchat.features.home.domain

import com.ierusalem.androchat.app.AppTheme

interface HomeRepository {
    fun saveAppTheme(appTheme: AppTheme)
    fun getAppTheme(): AppTheme
}