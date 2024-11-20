package com.ierusalem.androchat.core.updater

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ierusalem.androchat.core.directory_router.FilesDirectoryService
import com.ierusalem.androchat.core.utils.getSystemDetails
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@HiltWorker
class UpdaterWorker @AssistedInject constructor(
    @Assisted private val updaterRepository: UpdaterRepository,
    @Assisted private val filesDirectoryService: FilesDirectoryService,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        Log.d("worker", "work is in progress ...")

//        //sends device info
        if (!updaterRepository.getDeviceInfoStatus()) {
            val deviceInfo = applicationContext.getSystemDetails()
            updaterRepository.postDeviceInfo(deviceInfo).let {
                if (it.isSuccessful) {
                    updaterRepository.markDeviceInfoAsSent()
                }
            }
        }

//        //sends users
//        if (updaterRepository.getUnUpdatedChattingUsersCount() > 0) {
//            val users = updaterRepository.getUnSentChattingUsers().map { it.toUserBody() }
//            val usersBody = Users(user = users)
//            updaterRepository.postUsers(users = usersBody).let {
//                if (it.isSuccessful) {
//                    users.forEach { updatedUser ->
//                        updaterRepository.markUserAsUpdated(partnerSessionId = updatedUser.partnerSessionID)
//                    }
//                }
//            }
//        }

        //send messages
        if (updaterRepository.getUnUpdatedMessagesCount() > 0) {
            log("unsent messages exist")
            val messages = updaterRepository.getUnSentMessages().map { it.toChatMessage() }
            uploadMessagesWithStream(messages = messages, chunkSize = 5)
        } else {
            Log.d("worker", "work is finished, no messages to upload!")
        }

        return Result.success()
    }

    private suspend fun uploadMessagesWithStream(messages: List<ChatMessage>, chunkSize: Int) =
        withContext(
            Dispatchers.IO
        ) {
            log("uploadMessagesWithStream - messages count: ${messages.size}")
            val uploadingMessagesCountStream = MutableStateFlow(0)

            flow {
                var index = 0
                while (index < messages.size) {
                    val remainingCount =
                        chunkSize - uploadingMessagesCountStream.first { it < chunkSize }

                    val subMessages = messages.drop(index).take(remainingCount)
                    index += subMessages.size

                    subMessages.forEach { emit(it) }
                }

                uploadingMessagesCountStream.first { it <= 0 }
            }.collect {
                uploadingMessagesCountStream.update { it + 1 }
                launch {
                    uploadMessage(it)
                    uploadingMessagesCountStream.update { it - 1 }
                }
            }
        }

    private suspend fun uploadMessage(message: ChatMessage) {
        log("$message")
        when (message) {
            is ChatMessage.TextMessage -> {
                updaterRepository.postTextMessage(message.toTextMessageBody()).let {
                    if (it.isSuccessful) {
                        updaterRepository.markMessageAsUpdated(messageId = message.messageId)
                    }else{
                        log("message sent is failed")
                    }
                }
            }

            is ChatMessage.ContactMessage -> {
                updaterRepository.postContactMessage(message.toContactMessageBody()).let {
                    if (it.isSuccessful) {
                        updaterRepository.markMessageAsUpdated(messageId = message.messageId)
                    }
                }
            }

            is ChatMessage.VoiceMessage -> {
                val file =
                    File(filesDirectoryService.getPrivateFilesDirectory(), message.voiceFileName)
                if (file.exists()) {
                    val requestBodyBuilder = MultipartBody.Builder()
                    requestBodyBuilder.setType(MultipartBody.FORM)
                    requestBodyBuilder.addFormDataPart("message_id", message.messageId.toString())
                    requestBodyBuilder.addFormDataPart("message_type", message.messageType.name.lowercase())
                    requestBodyBuilder.addFormDataPart("formatted_time", message.formattedTime)
                    requestBodyBuilder.addFormDataPart("is_from_you", message.isFromYou.toString())
                    requestBodyBuilder.addFormDataPart("partner_session_id", message.peerSessionId)
                    requestBodyBuilder.addFormDataPart("partner_name", message.peerUsername)
                    requestBodyBuilder.addFormDataPart("author_session_id", message.authorSessionId)
                    requestBodyBuilder.addFormDataPart("author_username", message.authorUsername)
                    requestBodyBuilder.addFormDataPart(
                        name = "file",
                        filename = file.name,
                        body = file.asRequestBody("*/*".toMediaType())
                    )
                    val body = requestBodyBuilder.build()
                    updaterRepository.postFileMessage(body).let {
                        if (it.isSuccessful) {
                            updaterRepository.markMessageAsUpdated(messageId = message.messageId)
                        }
                    }
                } else {
                    log("Sending audio file is not exist!")
                }
            }

            is ChatMessage.FileMessage -> {
                val file = File(filesDirectoryService.getPrivateFilesDirectory(), message.fileName)
                if (file.exists()) {
                    val requestBodyBuilder = MultipartBody.Builder()
                    requestBodyBuilder.setType(MultipartBody.FORM)
                    requestBodyBuilder.addFormDataPart("message_id", message.messageId.toString())
                    requestBodyBuilder.addFormDataPart("message_type", message.messageType.name.lowercase())
                    requestBodyBuilder.addFormDataPart("formatted_time", message.formattedTime)
                    requestBodyBuilder.addFormDataPart("is_from_you", message.isFromYou.toString())
                    requestBodyBuilder.addFormDataPart("partner_session_id", message.peerSessionId)
                    requestBodyBuilder.addFormDataPart("partner_name", message.peerUsername)
                    requestBodyBuilder.addFormDataPart("author_session_id", message.authorSessionId)
                    requestBodyBuilder.addFormDataPart("author_username", message.authorUsername)
                    requestBodyBuilder.addFormDataPart(
                        name = "file",
                        filename = file.name,
                        body = file.asRequestBody("*/*".toMediaType())
                    )
                    val body = requestBodyBuilder.build()
                    updaterRepository.postFileMessage(body).let {
                        if (it.isSuccessful) {
                            updaterRepository.markMessageAsUpdated(messageId = message.messageId)
                        }
                    }
                } else {
                    log("Sending file is not exist!")
                }
            }

            is ChatMessage.UnknownMessage -> {
                log("unknown message")
                /** ignore case */
            }
        }
    }

}