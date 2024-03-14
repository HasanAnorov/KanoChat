package com.ierusalem.androchat.auth.login

sealed interface LoginNavigation {
    data object NavigateToMain: LoginNavigation
}