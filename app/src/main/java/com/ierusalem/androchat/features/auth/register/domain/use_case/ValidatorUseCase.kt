package com.ierusalem.androchat.features.auth.register.domain.use_case

import com.ierusalem.androchat.utils.Constants

class ValidatorUseCase {

    fun validateFirstName(firstName: String): ValidationResult {
        return when {
            firstName.isBlank() -> ValidationResult(
                successful = false,
                errorMessage = "The login can't be blank"
            )

            firstName.length < Constants.MINIMUM_LOGIN_LENGTH -> ValidationResult(
                successful = false,
                errorMessage = "Login should be than 3 words!"
            )

            else -> ValidationResult(
                successful = true,
            )
        }
    }

    fun validateLastName(lastName: String): ValidationResult {
        return when {
            lastName.isBlank() -> ValidationResult(
                successful = false,
                errorMessage = "The login can't be blank"
            )

            lastName.length < Constants.MINIMUM_LOGIN_LENGTH -> ValidationResult(
                successful = false,
                errorMessage = "Login should be than 3 words!"
            )

            else -> ValidationResult(
                successful = true,
            )
        }
    }

    fun validateLogin(login: String): ValidationResult {
        return when {
            login.isBlank() -> ValidationResult(
                successful = false,
                errorMessage = "The login can't be blank"
            )

            login.length < Constants.MINIMUM_LOGIN_LENGTH -> ValidationResult(
                successful = false,
                errorMessage = "Login should be than 3 words!"
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