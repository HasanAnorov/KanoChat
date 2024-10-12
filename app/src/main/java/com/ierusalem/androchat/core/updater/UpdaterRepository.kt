package com.ierusalem.androchat.core.updater

import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import okhttp3.RequestBody
import retrofit2.Response

interface UpdaterRepository {
    suspend fun getUnUpdatedMessagesCount(): Int
    suspend fun getUnSentMessages(): List<ChatMessageEntity>
    suspend fun postTextMessage(textMessageBody: TextMessageBody): Response<Unit>
    suspend fun postContactMessage(contactMessageBody: ContactMessageBody): Response<Unit>
    suspend fun postFileMessage(body: RequestBody): Response<Unit>
    suspend fun markMessageAsUpdated(messageId: Long)
}