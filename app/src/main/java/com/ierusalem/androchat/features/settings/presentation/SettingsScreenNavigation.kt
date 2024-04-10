package com.ierusalem.androchat.features.settings.presentation

sealed interface SettingsScreenNavigation {
    data object NavIconClick: SettingsScreenNavigation
}