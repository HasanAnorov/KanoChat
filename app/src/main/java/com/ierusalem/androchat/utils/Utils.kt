package com.ierusalem.androchat.utils

import android.util.Log
import java.util.Random

/**
 * Created by andro H.A 31.05.2024
 */

//Logs message in debug mode
fun log(message: String) {
    Log.d("ahi3646", message)
}

//Checks if port number is valid
fun isValidPortNumber(portNumber: String): Boolean {
    return portNumber.isNotEmpty() && portNumber.toInt() in Constants.MAX_PORT_NUMBER downTo Constants.MIN_PORT_NUMBER
}

//Generates random password for group connection
fun generateRandomPassword(length: Int = 8): String {
    val random = Random()
    val characters = Constants.CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION

    return (1..length)
        .map { characters[random.nextInt(characters.length)] }
        .joinToString("")
}