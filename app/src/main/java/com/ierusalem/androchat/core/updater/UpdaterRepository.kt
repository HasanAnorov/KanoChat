package com.ierusalem.androchat.core.updater

import retrofit2.Response

interface UpdaterRepository {

    suspend fun getUnUpdatedMessagesCount(): Int

    suspend fun postTextMessage(
        accessToken: String,
        textMessageBody: TextMessageBody
    ): Response<Unit>

}