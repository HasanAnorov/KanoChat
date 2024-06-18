package com.ierusalem.androchat.core.utils

import android.util.Log
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.features_tcp.server.IP_ADDRESS_REGEX
import java.util.Random

//Logs message in debug mode
fun log(message: String) {
    Log.d("ahi3646", message)
}

//Checks if port number is valid
fun isValidPortNumber(portNumber: String): Boolean {
    return portNumber.isNotEmpty() && portNumber.toInt() in Constants.MAX_PORT_NUMBER downTo Constants.MIN_PORT_NUMBER
}

fun isValidIpAddress(groupAddress: String): Boolean {
    return IP_ADDRESS_REGEX.matches(groupAddress)
}

fun isValidHotspotName(hotspotName: String): Boolean {
    return hotspotName.isNotEmpty() && hotspotName.length <= Constants.MAX_HOTSPOT_NAME_LENGTH && hotspotName.length >= Constants.MIN_HOTSPOT_NAME_LENGTH
}

//generates random password for group connection
fun generateRandomPassword(length: Int = 8): String {
    val random = Random()
    val characters = Constants.CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION

    return (1..length)
        .map { characters[random.nextInt(characters.length)] }
        .joinToString("")
}