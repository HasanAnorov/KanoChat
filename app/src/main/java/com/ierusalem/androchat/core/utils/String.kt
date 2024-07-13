package com.ierusalem.androchat.core.utils

/**
 * returns empty string if file extension is not found
 */
fun String.getExtensionFromFilename(): String {
    return if (this.lastIndexOf(".") > 0) {
        this.substringAfterLast(".")
    } else {
        ""
    }
}

fun String.getFileNameWithoutExtension(): String {
    val lastDotIndex = this.lastIndexOf('.')
    return if (lastDotIndex > 0) {
        this.substring(0, lastDotIndex)
    } else {
        this // No extension found, return the original fileName
    }
}

