package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity

import android.net.Uri
import androidx.annotation.Keep
import com.ierusalem.androchat.core.app.AppMessageType

@Keep
sealed class ChatMessage(
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
    ) : ChatMessage(username, formattedTime, isFromYou, messageType)

    data class FileMessage(
        override val formattedTime: String,
        override val username: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.FILE,
        val filePath: Uri,
        val fileName: String,
        val fileSize: String,
        val fileExtension: String,
        val fileState: FileMessageState = FileMessageState.Loading(0)
    ) : ChatMessage(username, formattedTime, isFromYou, messageType)

    data class ContactMessage(
        override val formattedTime: String,
        override val username: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.CONTACT,
        val contactName: String,
        val contactNumber: String
    ) : ChatMessage(
        username,
        formattedTime,
        isFromYou,
        messageType
    )

}


