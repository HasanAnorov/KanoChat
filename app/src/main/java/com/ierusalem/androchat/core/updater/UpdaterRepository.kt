package com.ierusalem.androchat.core.updater

import retrofit2.Response

interface UpdaterRepository {

    suspend fun postTextMessage(
        accessToken: String,
        textMessageBody: TextMessageBody
    ): Response<Unit>

}