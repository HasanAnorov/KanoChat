package com.ierusalem.androchat.features_local.tcp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.features_local.tcp.data.db.converters.FileMessageStateConverter
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: AppMessageType,
    val formattedTime: String,
    val isFromYou: Boolean,
    val peerUniqueId: String,
    val authorUniqueId: String,
    //text message specific parameters
    val text: String? = null,
    //voice message specific parameters
    val voiceMessageFileName: String? = null,
    val voiceMessageAudioFileDuration: Long? = null,
    //file message specific parameters
    @TypeConverters(FileMessageStateConverter::class)
    val fileState: FileMessageState? = null,
    val filePath: String? = null,
    val fileSize: String? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,
    val isFileAvailable: Boolean = false,
    //contact message specific parameters
    val contactName: String? = null,
    val contactNumber: String? = null
) {

    fun toChatMessage(peerUsername:String): ChatMessage? {
        return when (type) {
            AppMessageType.TEXT -> {
                text?.let {
                    ChatMessage.TextMessage(
                        formattedTime = formattedTime,
                        isFromYou = isFromYou,
                        messageType = type,
                        message = text,
                        messageId = id,
                        peerUsername = peerUsername
                    )
                }
            }

            AppMessageType.VOICE -> {
                voiceMessageFileName?.let {
                    ChatMessage.VoiceMessage(
                        messageType = type,
                        isFromYou = isFromYou,
                        formattedTime = formattedTime,
                        voiceFileName = voiceMessageFileName,
                        duration = voiceMessageAudioFileDuration!!,
                        fileState = fileState ?: FileMessageState.Failure,
                        messageId = id,
                        peerUsername = peerUsername
                    )
                }
            }

            AppMessageType.FILE -> {

                ChatMessage.FileMessage(
                    isFromYou = isFromYou,
                    formattedTime = formattedTime,
                    messageType = type,
                    filePath = filePath!!,
                    fileName = fileName!!,
                    fileSize = fileSize!!,
                    fileExtension = fileExtension!!,
                    fileState = fileState!!,
                    messageId = id,
                    peerUsername = peerUsername,
                    isFileMessageAvailable = isFileAvailable
                )
            }

            AppMessageType.CONTACT -> {
                ChatMessage.ContactMessage(
                    isFromYou = isFromYou,
                    formattedTime = formattedTime,
                    messageType = type,
                    contactName = contactName!!,
                    contactNumber = contactNumber!!,
                    messageId = id,
                    peerUsername = peerUsername
                )
            }

            else -> {
                null
            }
        }
    }
}