package com.ierusalem.androchat.features.auth.register.domain.model

import android.net.Uri

sealed class Message(
    open val username: String,
    open val formattedTime: String,
    open val isFromYou:Boolean
) {
    data class TextMessage(
        //todo - maybe this username field should be removed
        override val username: String,
        override val isFromYou: Boolean,
        val message: String,
        override val formattedTime: String
    ) : Message(username, formattedTime, isFromYou)

    data class FileMessage(
        override val formattedTime: String,
        override val username: String,
        override val isFromYou: Boolean,
        val filePath: Uri,
        val fileName: String,
        val fileSize: String,
        val fileExtension: String,
        val fileState: FileState = FileState.Loading(0)
    ) : Message(username, formattedTime, isFromYou)

}

sealed interface FileState {
    data class Loading(val percentage: Int) : FileState
    data object Success : FileState
    data object Failure : FileState
}

