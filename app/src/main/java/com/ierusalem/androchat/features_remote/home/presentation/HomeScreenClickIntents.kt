package com.ierusalem.androchat.features_remote.home.presentation

sealed interface HomeScreenClickIntents {
    data class TabItemClicked(val tabIndex: Int): HomeScreenClickIntents
    data object NavIconClicked: HomeScreenClickIntents
    data object DrawerSettingClick: HomeScreenClickIntents
    data object ListItemClicked: HomeScreenClickIntents
    data object OnSearchClick: HomeScreenClickIntents
    data object OnTcpClick: HomeScreenClickIntents
}