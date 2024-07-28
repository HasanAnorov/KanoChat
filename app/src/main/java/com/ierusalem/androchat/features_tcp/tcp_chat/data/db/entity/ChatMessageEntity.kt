package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.readableFileSize
import java.io.File

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: AppMessageType,
    val formattedTime: String,

    val isFromYou: Boolean,
    val userId: String,

    val text: String? = null,

    val voiceMessageName: String? = null,

    val fileState: FileMessageState = FileMessageState.Loading(0),
    val filePath: String? = null,
    val fileSize: String? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,

    val contactName: String? = null,
    val contactNumber: String? = null
) {

    fun toChatMessage(): ChatMessage? {
        return when (type) {
            AppMessageType.TEXT -> {
                text?.let {
                    ChatMessage.TextMessage(
                        formattedTime = formattedTime,
                        isFromYou = isFromYou,
                        messageType = type,
                        message = text,
                        messageId = id
                    )
                }
            }

            AppMessageType.VOICE -> {
                voiceMessageName?.let {
                    val voiceMessageAudioFile = File(voiceMessageName)
                    ChatMessage.VoiceMessage(
                        messageType = type,
                        isFromYou = isFromYou,
                        formattedTime = formattedTime,
                        filePath = voiceMessageAudioFile.path,
                        fileName = voiceMessageAudioFile.name,
                        fileSize = voiceMessageAudioFile.length().readableFileSize(),
                        fileExtension = voiceMessageAudioFile.extension,
                        duration = voiceMessageAudioFile.getAudioFileDuration(),
                        fileState = FileMessageState.Success,
                        messageId = id
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
                    fileState = fileState,
                    messageId = id
                )
            }

            AppMessageType.CONTACT -> {
                ChatMessage.ContactMessage(
                    isFromYou = isFromYou,
                    formattedTime = formattedTime,
                    messageType = type,
                    contactName = contactName!!,
                    contactNumber = contactNumber!!,
                    messageId = id
                )
            }

            else -> {
                null
            }
        }
    }
}