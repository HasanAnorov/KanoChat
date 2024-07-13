package com.ierusalem.androchat.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import java.util.Locale


//TODO user this later
fun String.toMessage(gson: Gson): Message.TextMessage {
    return gson.fromJson(this, Message.TextMessage::class.java)
}

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

