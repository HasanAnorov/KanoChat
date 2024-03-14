package com.ierusalem.androchat.features.auth.register.domain.use_case

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)