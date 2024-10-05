package com.ierusalem.androchat.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.ierusalem.androchat.core.utils.Constants.generateUniqueFileName
import java.io.File
import java.io.FileOutputStream
import java.util.Random

fun log(message: String) {
    Log.d("ahi3646", message)
}

fun isValidPortNumber(portNumber: String): Boolean {
    return portNumber.isNotEmpty() && portNumber.toInt() in Constants.MAX_PORT_NUMBER downTo Constants.MIN_PORT_NUMBER
}

fun isValidIpAddress(groupAddress: String): Boolean {
    return IP_ADDRESS_REGEX.matches(groupAddress)
}

fun isValidHotspotName(hotspotName: String): Boolean {
    return hotspotName.isNotEmpty() && hotspotName.length <= Constants.MAX_HOTSPOT_NAME_LENGTH && hotspotName.length >= Constants.MIN_HOTSPOT_NAME_LENGTH
}

fun isValidHotspotPassword(hotspotPassword: String): Boolean {
    return hotspotPassword.isNotEmpty() && hotspotPassword.length <= Constants.MAX_HOTSPOT_PASSWORD_LENGTH && hotspotPassword.length >= Constants.MIN_HOTSPOT_PASSWORD_LENGTH
}

fun generateRandomPassword(length: Int = 8): String {
    val random = Random()
    val characters = Constants.CHARACTERS_SET_FOR_RAND0M_PASSWORD_GENERATION

    return (1..length)
        .map { characters[random.nextInt(characters.length)] }
        .joinToString("")
}

fun getFileByName(fileName: String, resourceDirectory: File): File {
    if (!resourceDirectory.exists()) {
        resourceDirectory.mkdir()
        log("Directory not found: created directory: ${resourceDirectory.absolutePath}")
    }

    var file = File(resourceDirectory, fileName)
    if (file.exists()) {
        log("same file found in folder, generating unique name ...")
        val fileNameWithoutExt = fileName.getFileNameWithoutExtension()
        val fileExtension = fileNameWithoutExt.getExtensionFromFilename()
        val uniqueFileName =
            generateUniqueFileName(resourceDirectory.toString(), fileNameWithoutExt, fileExtension)
        log("unique file name - $uniqueFileName")
        file = File(uniqueFileName)
    }
    return file
}

/**
 * What the f*ck is this
 * https://stackoverflow.com/questions/10006459/regular-expression-for-ip-address-validation
 *
 * Tests if a given string is an IP address
 */
@JvmField
val IP_ADDRESS_REGEX =
    """^(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))$"""
        .toRegex()

fun generateFileFromUri(contentResolver: ContentResolver, uri: Uri, resourceDirectory: File): File {
    if (!resourceDirectory.exists()) {
        resourceDirectory.mkdir()
    }
    val fileName = uri.getFileNameFromUri(contentResolver)
    val fileNameWithLabel = fileName.addLabelBeforeExtension()
    var file = File(resourceDirectory, fileNameWithLabel)
    if (file.exists()) {
        val fileNameWithoutExt = fileNameWithLabel.getFileNameWithoutExtension()
        val uniqueFileName =
            generateUniqueFileName(
                resourceDirectory.toString(),
                fileNameWithoutExt,
                file.extension
            )
        file = File(uniqueFileName)
    }

    val inputStream = contentResolver.openInputStream(uri)
    val fileOutputStream = FileOutputStream(file)
    inputStream?.copyTo(fileOutputStream)
    fileOutputStream.close()
    return file
}
