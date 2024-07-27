package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity


import android.os.Environment
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.utils.getAudioFileDuration
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
    val fileMessage: FileMessage? = null,
    val voiceMessageName: String? = null,
    val contactInfo: ContactMessageEntity? = null
) {

    //todo add time and file size
    data class FileMessage(
        val fileState: FileMessageState = FileMessageState.Loading(0),
        val filePath: String,
    )

    data class ContactMessageEntity(
        val contactName: String,
        val contactNumber: String
    )

    fun toChatMessage(): ChatMessage? {
        val resourceDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
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
                    val voiceMessageAudioFile = File(resourceDirectory, voiceMessageName)
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
                fileMessage?.let {
                    val file = File( fileMessage.filePath)
                    ChatMessage.FileMessage(
                        isFromYou = isFromYou,
                        formattedTime = formattedTime,
                        messageType = type,
                        filePath = file.path,
                        fileName = file.name,
                        fileSize = file.length().readableFileSize(),
                        fileExtension = file.extension,
                        fileState = fileMessage.fileState,
                        messageId = id
                    )
                }
            }

            AppMessageType.CONTACT -> {
                contactInfo?.let {
                    ChatMessage.ContactMessage(
                        isFromYou = isFromYou,
                        formattedTime = formattedTime,
                        messageType = type,
                        contactName = it.contactName,
                        contactNumber = it.contactNumber,
                        messageId = id
                    )
                }
            }

            else -> {
                null
            }
        }
    }
}