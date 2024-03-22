package com.ierusalem.androchat.features.auth.register.presentation

sealed interface RegistrationNavigation {
    data object ToHome : RegistrationNavigation
    data object ToLogin : RegistrationNavigation
}