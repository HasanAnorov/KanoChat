package com.ierusalem.androchat.features_common.auth.login.presentation

sealed interface LoginNavigation {
    data object ToHome: LoginNavigation
    data object ToRegister: LoginNavigation
}