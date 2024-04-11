package com.ierusalem.androchat.features.auth.login.presentation

sealed interface LoginNavigation {
    data object ToHome: LoginNavigation
    data object ToRegister: LoginNavigation
}