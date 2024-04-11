package com.ierusalem.androchat.features.home.presentation

sealed interface HomeScreenClickIntents {
    data class TabItemClicked(val tabIndex: Int): HomeScreenClickIntents
    data object NavIconClicked: HomeScreenClickIntents
    data object DrawerSettingClick: HomeScreenClickIntents
    data object ListItemClicked: HomeScreenClickIntents
}