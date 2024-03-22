package com.ierusalem.androchat.features.auth.login.domain

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.features.auth.login.presentation.LoginFormEvents
import com.ierusalem.androchat.features.auth.login.presentation.LoginNavigation
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel(), DefaultLifecycleObserver,
    NavigationEventDelegate<LoginNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<LoginScreenState> = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun handleEvents(event: LoginFormEvents){
        when(event){
            LoginFormEvents.Login -> loginUser()
            LoginFormEvents.ToRegister -> emitNavigation(LoginNavigation.ToHome)
            is LoginFormEvents.UsernameChanged -> {
                _state.update {
                    it.copy(
                        username = event.username
                    )
                }
            }
            is LoginFormEvents.PasswordChanged -> {
                _state.update {
                    it.copy(
                        password = event.password
                    )
                }
            }
            LoginFormEvents.PasswordVisibilityChanged -> {
                _state.update {
                    it.copy(
                        passwordVisibility = !state.value.passwordVisibility
                    )
                }
            }
        }
    }

    private fun loginUser(){
        Log.d("ahi3646", "loginUser: ")
    }

}

@Immutable
data class LoginScreenState(
    val username: String = "",
    val usernameError: String? = "",
    val password: String = "",
    val passwordError: String? = "",
    val passwordVisibility: Boolean = false
)