package com.ierusalem.androchat.core.updater

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ierusalem.androchat.core.directory_router.FilesDirectoryService
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
        if (updaterRepository.getUnUpdatedMessagesCount() > 0) {
            log("unsent messages exist")
            val messages = updaterRepository.getUnSentMessages().map { it.toChatMessage() }
            uploadMessagesWithStream(messages = messages, chunkSize = 5)
            return Result.success()
        } else {
            Log.d("worker", "work is finished, no messages to upload!")
            return Result.success()
        }
    }

    private suspend fun uploadMessagesWithStream(messages: List<ChatMessage>, chunkSize: Int) =
        withContext(
            Dispatchers.IO
        ) {
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
        print("$message ")
        when (message) {
            is ChatMessage.TextMessage -> {
                updaterRepository.postTextMessage(message.toTextMessageBody()).let {
                    if (it.isSuccessful) {
                        updaterRepository.markMessageAsUpdated(messageId = message.messageId)
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
                    requestBodyBuilder.addFormDataPart("messageId", message.messageId.toString())
                    requestBodyBuilder.addFormDataPart("messageType", message.messageType.name)
                    requestBodyBuilder.addFormDataPart("formattedTime", message.formattedTime)
                    requestBodyBuilder.addFormDataPart("isFromYou", message.isFromYou.toString())
                    requestBodyBuilder.addFormDataPart("partnerSessionId", message.peerSessionId)
                    requestBodyBuilder.addFormDataPart("partnerName", message.peerUsername)
                    requestBodyBuilder.addFormDataPart("authorSessionId", message.authorSessionId)
                    requestBodyBuilder.addFormDataPart("authorUsername", message.authorUsername)
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
                    requestBodyBuilder.addFormDataPart("messageId", message.messageId.toString())
                    requestBodyBuilder.addFormDataPart("messageType", message.messageType.name)
                    requestBodyBuilder.addFormDataPart("formattedTime", message.formattedTime)
                    requestBodyBuilder.addFormDataPart("isFromYou", message.isFromYou.toString())
                    requestBodyBuilder.addFormDataPart("partnerSessionId", message.peerSessionId)
                    requestBodyBuilder.addFormDataPart("partnerName", message.peerUsername)
                    requestBodyBuilder.addFormDataPart("authorSessionId", message.authorSessionId)
                    requestBodyBuilder.addFormDataPart("authorUsername", message.authorUsername)
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
                /** ignore case */
            }
        }
    }

}