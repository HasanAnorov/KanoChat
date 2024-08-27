package com.ierusalem.androchat.features_common.auth.login.presentation


sealed class LoginFormEvents {
    data class UsernameChanged(val username: String) : LoginFormEvents()
    data class PasswordChanged(val password: String) : LoginFormEvents()
    data object Login : LoginFormEvents()
    data object ToRegister : LoginFormEvents()
    data object PasswordVisibilityChanged: LoginFormEvents()
}