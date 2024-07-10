package com.ierusalem.androchat.features.auth.register.domain.model

import android.net.Uri

sealed class Message(
    open val username: String,
    open val formattedTime: String
){
    data class TextMessage(
        override val username: String,
        val message:String,
        override val formattedTime: String
    ): Message(username, formattedTime)

    data class FileMessage(
        override val formattedTime: String,
        override val username: String,
        val filePath:Uri,
        val filename:String,
    ): Message(username, formattedTime)
}

