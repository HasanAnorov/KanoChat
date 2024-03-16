package com.ierusalem.androchat.features.home.presentation

sealed interface HomeScreenNavigation {
    data object NavigateToPrivate: HomeScreenNavigation
    data object NavigateToGroup: HomeScreenNavigation
}