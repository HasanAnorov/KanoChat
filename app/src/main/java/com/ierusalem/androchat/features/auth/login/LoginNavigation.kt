package com.ierusalem.androchat.features.auth.login

sealed interface LoginNavigation {
    data object NavigateToMain: LoginNavigation
}