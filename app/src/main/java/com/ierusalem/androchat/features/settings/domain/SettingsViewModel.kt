package com.ierusalem.androchat.features.settings.domain

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.features.settings.presentation.SettingsScreenEvents
import com.ierusalem.androchat.features.settings.presentation.SettingsScreenNavigation
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel: ViewModel(),
    NavigationEventDelegate<SettingsScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    fun handleEvents(event: SettingsScreenEvents){
        when(event){
            SettingsScreenEvents.NavIconClick -> {
                emitNavigation(SettingsScreenNavigation.NavIconClick)
            }
            SettingsScreenEvents.OnAppThemeChange -> {}
            SettingsScreenEvents.OnLanguageChange -> {}
        }
    }

}

@Immutable
data class SettingsState(
    val language: String = ""
)