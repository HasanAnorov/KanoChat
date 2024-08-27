package com.ierusalem.androchat.features_remote.home.presentation

sealed interface HomeScreenNavigation {
    data object NavigateToPrivate: HomeScreenNavigation
    data object NavigateToGroup: HomeScreenNavigation
    data object NavigateToSettings: HomeScreenNavigation
    data object NavigateToTcp: HomeScreenNavigation
}