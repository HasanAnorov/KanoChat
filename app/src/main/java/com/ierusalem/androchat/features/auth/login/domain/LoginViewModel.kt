package com.ierusalem.androchat.features.auth.login.domain

import androidx.compose.runtime.Immutable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.FieldValidator
import com.ierusalem.androchat.features.auth.login.presentation.LoginFormEvents
import com.ierusalem.androchat.features.auth.login.presentation.LoginNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validator: FieldValidator,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository
) : ViewModel(), DefaultLifecycleObserver,
    NavigationEventDelegate<LoginNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<LoginScreenState> = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun handleEvents(event: LoginFormEvents){
        when(event){
            LoginFormEvents.Login -> loginUser()
            LoginFormEvents.ToRegister -> emitNavigation(LoginNavigation.ToRegister)
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

    private fun loginUser() {
        val usernameResult = validator.validateUsername(state.value.username)
        val passwordResult = validator.validatePassword(state.value.password)

        val hasError = listOf(
            usernameResult,
            passwordResult,
        ).any {
            !it.successful
        }

        if (hasError) {
            _state.update {
                it.copy(
                    usernameError = usernameResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                )
            }
            return
        }
        _state.update {
            it.copy(
                usernameError = null,
                passwordError = null,
            )
        }

        viewModelScope.launch {
            dataStorePreferenceRepository.setUsername(state.value.username)
            emitNavigation(LoginNavigation.ToHome)
        }
    }

}

@Immutable
data class LoginScreenState(
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordVisibility: Boolean = false
)