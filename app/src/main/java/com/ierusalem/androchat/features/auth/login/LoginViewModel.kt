package com.ierusalem.androchat.features.auth.login

import androidx.compose.runtime.Immutable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel(), DefaultLifecycleObserver,
    NavigationEventDelegate<LoginNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<LoginScreenState> = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun onUsernameChanged(userName: String) {
        _state.update {
            it.copy(userName = userName)
        }
    }

    fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(password = password)
        }
    }


}

@Immutable
data class LoginScreenState(
    val userName: String = "",
    val password: String = "",
)