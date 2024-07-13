package com.ierusalem.androchat.features.auth.register.domain.model

import android.net.Uri

sealed class Message(
    open val username: String,
    open val formattedTime: String
) {
    data class TextMessage(
        //this username field should be removed
        override val username: String,
        val message: String,
        override val formattedTime: String
    ) : Message(username, formattedTime)

    data class FileMessage(
        override val formattedTime: String,
        override val username: String,
        val filePath: Uri,
        val filename: String,
        val fileSize: String,
        val fileExtension: String,
        val fileState: FileState = FileState.Loading(0)
    ) : Message(username, formattedTime)
}

sealed interface FileState {
    data class Loading(val percentage: Int) : FileState
    data object Success : FileState
    data object Failure : FileState
}

