package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.core.app.AppMessageType

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: AppMessageType,
    val formattedTime: String,
    val isFromYou: Boolean,
    val userId: String,
    //text message specific parameters
    val text: String? = null,
    //voice message specific parameters
    val voiceMessageFileName: String? = null,
    val voiceMessageAudioFileDuration: Long? = null,
    //file message specific parameters
    val fileState: FileMessageState = FileMessageState.Loading(0),
    val filePath: String? = null,
    val fileSize: String? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,
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
                        fileState = FileMessageState.Success,
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
                    fileState = fileState,
                    messageId = id,
                    peerUsername = peerUsername
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