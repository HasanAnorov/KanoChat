package com.ierusalem.androchat.features_common.settings.presentation

import com.ierusalem.androchat.core.app.AppLanguage
import com.ierusalem.androchat.core.app.BroadcastFrequency

sealed interface SettingsScreenEvents {
    data object NavIconClick : SettingsScreenEvents
    data object LanguageCLick:SettingsScreenEvents
    data class OnBroadcastFrequencyChange(val broadcastFrequency: BroadcastFrequency):SettingsScreenEvents
    data class OnLanguageChange(val language: AppLanguage) : SettingsScreenEvents
    data object OnThemeChange : SettingsScreenEvents
    data object OnDismissLanguageDialog : SettingsScreenEvents
}
