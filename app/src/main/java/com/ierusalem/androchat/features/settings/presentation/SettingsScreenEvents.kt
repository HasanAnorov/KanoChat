package com.ierusalem.androchat.features.settings.presentation

sealed interface SettingsScreenEvents {
    data object NavIconClick: SettingsScreenEvents
    data object OnLanguageChange: SettingsScreenEvents
    data object OnAppThemeChange: SettingsScreenEvents
}