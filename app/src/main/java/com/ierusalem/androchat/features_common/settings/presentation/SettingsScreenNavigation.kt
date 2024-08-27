package com.ierusalem.androchat.features_common.settings.presentation

sealed interface SettingsScreenNavigation {
    data object NavIconClick: SettingsScreenNavigation
}