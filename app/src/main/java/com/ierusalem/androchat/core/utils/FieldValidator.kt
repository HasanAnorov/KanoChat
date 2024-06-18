package com.ierusalem.androchat.core.utils

import com.ierusalem.androchat.core.constants.Constants


class FieldValidator {

    fun validateUsername(login: String): ValidationResult {
        return when {
            login.isBlank() -> ValidationResult(
                successful = false,
                errorMessage = "The username can't be blank"
            )

            login.length < Constants.MINIMUM_LOGIN_LENGTH -> ValidationResult(
                successful = false,
                errorMessage = "Username should be than 3 words!"
            )

            else -> ValidationResult(
                successful = true,
            )
        }
    }

    fun validatePassword(password: String): ValidationResult {
        val containsLetterAndDigits = password.any {
            it.isDigit()
        } && password.any {
            it.isLetter()
        }
        return when {
            password.isBlank() -> ValidationResult(
                successful = false,
                errorMessage = "The login can't be blank"
            )

            password.length < Constants.MINIMUM_LOGIN_LENGTH -> ValidationResult(
                successful = false,
                errorMessage = "Login should be than 3 characters!"
            )

            !containsLetterAndDigits -> {
                ValidationResult(
                    successful = false,
                    errorMessage = "Password must be include digits and letters"
                )
            }

            else -> ValidationResult(
                successful = true,
            )
        }
    }

    fun validateRepeatedPassword(password: String, repeatedPassword: String): ValidationResult {
        if(password != repeatedPassword){
            return ValidationResult(
                successful = false,
                errorMessage = "The passwords don't match"
            )
        }
        return ValidationResult(
            successful = true
        )
    }

}

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)