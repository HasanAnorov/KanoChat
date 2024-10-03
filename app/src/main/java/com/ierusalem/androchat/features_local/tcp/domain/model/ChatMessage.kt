package com.ierusalem.androchat.features_local.tcp.domain.model

import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState

sealed interface ChatMessage {
    val messageId: Long
    val formattedTime: String
    val isFromYou: Boolean
    val messageType: AppMessageType
    val partnerUsername: String

    data class TextMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.TEXT,
        override val partnerUsername: String,
        val message: String
    ) : ChatMessage

    data class FileMessage(
        override val messageId: Long,
        override val formattedTime: String,
        override val isFromYou: Boolean,
        override val messageType: AppMessageType = AppMessageType.FILE,
        override val partnerUsername: String,
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
        override val partnerUsername: String,
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
        override val partnerUsername: String,
        val contactName: String,
        val contactNumber: String
    ) : ChatMessage

}

sealed interface AudioState {
    data object Idle : AudioState
    data class Playing(val timing: Long) : AudioState
    data class Paused(val currentPosition: Int) : AudioState
}


