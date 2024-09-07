package com.ierusalem.androchat.features_common.auth.login.domain

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.FieldValidator
import com.ierusalem.androchat.features_common.auth.login.presentation.LoginFormEvents
import com.ierusalem.androchat.features_common.auth.login.presentation.LoginNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    val visibleSnackbarMessagesQueue = mutableStateListOf<SnackBarMessage>()

    fun handleEvents(event: LoginFormEvents) {
        when (event) {
            LoginFormEvents.Login -> {
                viewModelScope.launch(Dispatchers.IO) {
                    loginUser()
                }
            }

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

    private suspend fun loginUser() {
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

        val deviceLogin: String = dataStorePreferenceRepository.getUsername.first()
        val devicePassword: String = dataStorePreferenceRepository.getPassword.first()

        if (deviceLogin.isNotEmpty() && devicePassword.isNotEmpty()) {
            if (deviceLogin == state.value.username && devicePassword == state.value.password) {
                emitNavigation(LoginNavigation.ToLocal)
            } else {
                //show credentials didn't match error
                visibleSnackbarMessagesQueue.add(
                    SnackBarMessage(
                        message = "Credentials didn't match",
                        actionLabel = "OK"
                    )
                )
            }
        } else {
            //there is no saved username or password register
            visibleSnackbarMessagesQueue.add(
                SnackBarMessage(
                    message = "There is no saved username or password. Register first",
                    actionLabel = "OK"
                )
            )
        }
    }

}

@Immutable
data class LoginScreenState(
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordVisibility: Boolean = false,
)

data class SnackBarMessage(
    val message: String,
    val actionLabel: String,
)