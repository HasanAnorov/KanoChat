package com.ierusalem.androchat.features_common.settings.presentation

import com.ierusalem.androchat.core.app.AppLanguage
import com.ierusalem.androchat.core.app.AppBroadcastFrequency

sealed interface SettingsScreenEvents {
    data object NavIconClick : SettingsScreenEvents
    data object LanguageCLick:SettingsScreenEvents
    data class OnBroadcastFrequencyChange(val broadcastFrequency: AppBroadcastFrequency):SettingsScreenEvents
    data class OnLanguageChange(val language: AppLanguage) : SettingsScreenEvents
    data object OnThemeChange : SettingsScreenEvents
    data object OnDismissLanguageDialog : SettingsScreenEvents
}
