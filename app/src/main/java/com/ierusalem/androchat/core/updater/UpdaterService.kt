package com.ierusalem.androchat.core.updater

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdaterService {

    @POST("text/")
    suspend fun postTextMessage(@Body textMessageBody: TextMessageBody): Response<Unit>

    @POST("contacts/")
    suspend fun postContactMessage(@Body contactMessageBody: ContactMessageBody): Response<Unit>

    @POST("file/")
    suspend fun postFileMessage(@Body body: RequestBody): Response<Unit>

    @POST("users/")
    suspend fun postUsers(@Body userBody: Users): Response<Unit>

}
data class Users(
    val user: List<UserBody>
)

data class UserBody(
    val partnerSessionID: String,
    val partnerUsername: String,
    val authorSessionId:String,
    val avatarBackgroundColor: Int,
    val createdAt: String
)

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

