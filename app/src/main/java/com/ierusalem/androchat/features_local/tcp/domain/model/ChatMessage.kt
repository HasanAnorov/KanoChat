package com.ierusalem.androchat.features_local.tcp.domain.model

import androidx.compose.runtime.Stable
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.updater.ContactMessageBody
import com.ierusalem.androchat.core.updater.TextMessageBody
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState

@Stable
sealed interface ChatMessage {
    val messageId: Long
    val formattedTime: String
    val isFromYou: Boolean
    val messageType: AppMessageType
    val peerUsername: String
    val peerSessionId: String
    val authorSessionId: String
    val authorUsername: String

    data class TextMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.TEXT,
        override val peerUsername: String,
        override val peerSessionId: String,
        override val authorSessionId: String,
        override val authorUsername: String,
        val message: String
    ) : ChatMessage {
        fun toTextMessageBody(): TextMessageBody {
            return TextMessageBody(
                messageId = messageId.toString(),
                messageType = messageType.name.lowercase(),
                formattedTime = formattedTime,
                isFromYou = isFromYou.toString(),
                partnerSessionId = peerSessionId,
                partnerName = peerUsername,
                authorSessionId = authorSessionId,
                authorUsername = authorUsername,
                text = message
            )
        }
    }

    data class FileMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.FILE,
        override val peerUsername: String,
        override val peerSessionId: String,
        override val authorSessionId: String,
        override val authorUsername: String,
        val filePath: String,
        val fileName: String,
        val fileSize: String,
        val fileExtension: String,
        val fileState: FileMessageState = FileMessageState.Loading(0),
        val isFileMessageAvailable: Boolean
    ) : ChatMessage

    data class VoiceMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.VOICE,
        override val peerUsername: String,
        override val peerSessionId: String,
        override val authorSessionId: String,
        override val authorUsername: String,
        val duration: Long,
        val voiceFileName: String,
        val fileState: FileMessageState = FileMessageState.Loading(0),
        val audioState: AudioState = AudioState.Idle
    ) : ChatMessage

    data class ContactMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.CONTACT,
        override val peerUsername: String,
        override val peerSessionId: String,
        override val authorSessionId: String,
        override val authorUsername: String,
        val contactName: String,
        val contactNumber: String
    ) : ChatMessage {
        fun toContactMessageBody(): ContactMessageBody {
            return ContactMessageBody(
                messageId = messageId.toString(),
                messageType = messageType.name.lowercase(),
                formattedTime = formattedTime,
                isFromYou = isFromYou.toString(),
                partnerSessionId = peerSessionId,
                partnerName = peerUsername,
                authorSessionId = authorSessionId,
                authorUsername = authorUsername,
                contactName = contactName,
                contactNumber = contactNumber
            )
        }
    }

    data class UnknownMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.UNKNOWN,
        override val peerUsername: String,
        override val peerSessionId: String,
        override val authorSessionId: String,
        override val authorUsername: String,
    ) : ChatMessage

}

sealed interface AudioState {
    data object Idle : AudioState
    data class Playing(val timing: Long) : AudioState
    data class Paused(val currentPosition: Int) : AudioState
}


