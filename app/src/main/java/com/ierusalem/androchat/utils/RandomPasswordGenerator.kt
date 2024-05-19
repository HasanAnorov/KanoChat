package com.ierusalem.androchat.utils

import java.util.Random

fun generateRandomPassword(length: Int = 8): String {
    val random = Random()
    val characters = Constants.CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION

    return (1..length)
        .map { characters[random.nextInt(characters.length)] }
        .joinToString("")
}