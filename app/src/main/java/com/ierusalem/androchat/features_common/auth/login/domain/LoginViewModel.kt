package com.ierusalem.androchat.features_common.auth.login.domain

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.R
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

            is LoginFormEvents.UsernameChanged -> {
                _state.update {
                    it.copy(
                        username = event.username
                    )
                }
            }
        }
    }

    private suspend fun loginUser() {
        val usernameResult = validator.validateUsername(state.value.username)

        val hasError = listOf(
            usernameResult,
        ).any {
            !it.successful
        }

        if (hasError) {
            _state.update {
                it.copy(
                    usernameError = usernameResult.errorMessage,
                )
            }
            return
        }
        _state.update {
            it.copy(
                usernameError = null,
            )
        }

        val deviceLogin: String = dataStorePreferenceRepository.getUsername.first()

        if (deviceLogin.isNotEmpty()) {
            if (deviceLogin == state.value.username) {
                dataStorePreferenceRepository.setLoggingStatus(true)
                emitNavigation(LoginNavigation.ToLocal)
            } else {
                //show credentials didn't match error
                visibleSnackbarMessagesQueue.add(
                    SnackBarMessage(
                        message = R.string.credentials_didn_t_match,
                        actionLabel = R.string.ok
                    )
                )
            }
        } else {
            //there is no saved username just login
            dataStorePreferenceRepository.setUsername(state.value.username)
            dataStorePreferenceRepository.setLoggingStatus(true)
            emitNavigation(LoginNavigation.ToLocal)
        }
    }

}

@Immutable
data class LoginScreenState(
    val username: String = "",
    val usernameError: String? = null,
)

data class SnackBarMessage(
    val message: Int,
    val actionLabel: Int,
)