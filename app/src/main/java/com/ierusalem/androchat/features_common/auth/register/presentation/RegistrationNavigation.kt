package com.ierusalem.androchat.features_common.auth.register.presentation

sealed interface RegistrationNavigation {
    data class ToHome(val username: String) : RegistrationNavigation
    data object ToLogin : RegistrationNavigation
}