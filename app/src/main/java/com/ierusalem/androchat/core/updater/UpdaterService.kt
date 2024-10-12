package com.ierusalem.androchat.core.updater

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UpdaterService {

    @POST("text/")
    suspend fun postTextMessage(@Body textMessageBody: TextMessageBody): Response<Unit>

    @POST("contacts/")
    suspend fun postContactMessage(@Body contactMessageBody: ContactMessageBody): Response<Unit>

    @POST("file/")
    suspend fun postFileMessage(@Body body: RequestBody): Response<Unit>

}

data class TextMessageBody(
    //default fields
    val messageId: String,
    val messageType: String,
    val formattedTime: String,
    val isFromYou: String,
    val partnerSessionId: String,
    val partnerName: String,
    val authorSessionId: String,
    val authorUsername: String,
    //text message specific fields
    val text: String
)

data class ContactMessageBody(
    //default fields
    val messageId: String,
    val messageType: String,
    val formattedTime: String,
    val isFromYou: String,
    val partnerSessionId: String,
    val partnerName: String,
    val authorSessionId: String,
    val authorUsername: String,
    //text message specific fields
    val contactName: String,
    val contactNumber: String
)

