package com.ierusalem.androchat.features.auth.register.domain.model

import android.net.Uri
import com.ierusalem.androchat.core.app.AppMessageType

sealed class Message(
    //todo - maybe this username field should be removed
    open val username: String,
    open val formattedTime: String,
    open val isFromYou: Boolean,
    open val messageType: AppMessageType
) {
    data class TextMessage(
        override val username: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.TEXT,
        val message: String,
        override val formattedTime: String
    ) : Message(username, formattedTime, isFromYou, messageType)

    data class FileMessage(
        override val formattedTime: String,
        override val username: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.FILE,
        val filePath: Uri,
        val fileName: String,
        val fileSize: String,
        val fileExtension: String,
        val fileState: FileState = FileState.Loading(0)
    ) : Message(username, formattedTime, isFromYou, messageType)

    data class ContactMessage(
        override val formattedTime: String,
        override val username: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.CONTACT,
        val contactName: String,
        val contactNumber: String
    ) : Message(
        username,
        formattedTime,
        isFromYou,
        messageType
    )

}


//todo - finish proper file handling ...
sealed interface FileState {
    data class Loading(val percentage: Int) : FileState
    data object Success : FileState
    data object Failure : FileState
}

