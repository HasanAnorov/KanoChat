package com.ierusalem.androchat.features_tcp.tcp.info.domain

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.features.home.domain.HomeScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InfoViewModel: ViewModel() {

    private val _state: MutableStateFlow<InfoScreenUiState> = MutableStateFlow(
        InfoScreenUiState()
    )
    val state = _state.asStateFlow()



}

@Immutable
data class InfoScreenUiState(
    val isPasswordVisible: Boolean = false
)