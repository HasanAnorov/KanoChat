package com.ierusalem.androchat.features.auth.register.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.utils.FieldValidator
import com.ierusalem.androchat.features.auth.register.presentation.RegistrationFormEvents
import com.ierusalem.androchat.features.auth.register.presentation.RegistrationNavigation
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val validator: FieldValidator
) : ViewModel(),
    NavigationEventDelegate<RegistrationNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<RegistrationScreenState> = MutableStateFlow(
        RegistrationScreenState()
    )
    val state = _state.asStateFlow()

    fun handleEvents(event: RegistrationFormEvents) {
        when (event) {
            is RegistrationFormEvents.FirstNameChanged -> {
                _state.update {
                    it.copy(
                        firstname = event.firstName
                    )
                }
            }

            is RegistrationFormEvents.LastNameChanged -> {
                _state.update {
                    it.copy(
                        lastname = event.lastName
                    )
                }
            }

            is RegistrationFormEvents.UsernameChanged -> {
                _state.update {
                    it.copy(
                        username = event.username
                    )
                }
            }

            is RegistrationFormEvents.PasswordChanged -> {
                _state.update {
                    it.copy(
                        password = event.password
                    )
                }
            }

            RegistrationFormEvents.PasswordVisibilityChanged -> {
                _state.update {
                    it.copy(
                        passwordVisibility = !state.value.passwordVisibility
                    )
                }
            }

            is RegistrationFormEvents.RepeatedPasswordChanged -> {
                _state.update {
                    it.copy(
                        repeatedPassword = event.repeatedPassword
                    )
                }
            }

            is RegistrationFormEvents.Register -> registerUser()

            RegistrationFormEvents.ToLogin -> {
                emitNavigation(RegistrationNavigation.ToLogin)
            }
        }
    }

    private fun registerUser() {
        Log.d("ahi3646", "registerUser: ")
        val firstNameResult = validator.validateFirstName(state.value.firstname)
        val lastNameResult = validator.validateLastName(state.value.lastname)
        val usernameResult = validator.validateUsername(state.value.username)
        val passwordResult = validator.validatePassword(state.value.password)
        val repeatedPasswordResult =
            validator.validateRepeatedPassword(state.value.password, state.value.repeatedPassword)

        val hasError = listOf(
            firstNameResult,
            lastNameResult,
            usernameResult,
            passwordResult,
            repeatedPasswordResult
        ).any {
            !it.successful
        }

        if (hasError) {
            _state.update {
                it.copy(
                    firstnameError = firstNameResult.errorMessage,
                    lastnameError = lastNameResult.errorMessage,
                    usernameError = usernameResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                    repeatedPasswordError = repeatedPasswordResult.errorMessage,
                )
            }
            return
        }
        _state.update {
            it.copy(
                firstnameError = null,
                lastnameError = null,
                usernameError = null,
                passwordError = null,
                repeatedPasswordError = null,
            )
        }
        viewModelScope.launch {
            emitNavigation(RegistrationNavigation.ToHome)
        }
    }
}

data class RegistrationScreenState(
    val firstname: String = "",
    val firstnameError: String? = null,
    val lastname: String = "",
    val lastnameError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val repeatedPassword: String = "",
    val repeatedPasswordError: String? = null,
    val passwordVisibility: Boolean = false
)