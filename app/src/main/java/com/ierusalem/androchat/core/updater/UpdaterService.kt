package com.ierusalem.androchat.core.updater

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UpdaterService {

    @POST("chats/")
    suspend fun postTextMessage(
        @Header("Authorization") accessToken: String,
        @Body body: TextMessageBody
    ): Response<Unit>

}

data class TextMessageBody(
    val text: String
)

