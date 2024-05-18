package com.ierusalem.androchat.features_tcp.tcp.tcp.domain

import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.features_tcp.tcp.info.domain.InfoScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TcpViewModel:ViewModel() {

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(
        TcpScreenUiState()
    )
    val state = _state.asStateFlow()



}

data class TcpScreenUiState(
    val isPasswordVisible: Boolean = false
)