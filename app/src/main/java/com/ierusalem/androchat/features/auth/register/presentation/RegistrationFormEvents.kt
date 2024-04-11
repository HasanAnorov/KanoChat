package com.ierusalem.androchat.features.auth.register.presentation

sealed class RegistrationFormEvents {
    data class FirstNameChanged(val firstName: String) : RegistrationFormEvents()
    data class LastNameChanged(val lastName: String) : RegistrationFormEvents()
    data class UsernameChanged(val username: String) : RegistrationFormEvents()
    data class PasswordChanged(val password: String) : RegistrationFormEvents()
    data class RepeatedPasswordChanged(val repeatedPassword: String) : RegistrationFormEvents()
    data object Register : RegistrationFormEvents()
    data object ToLogin: RegistrationFormEvents()
    data object PasswordVisibilityChanged: RegistrationFormEvents()
}