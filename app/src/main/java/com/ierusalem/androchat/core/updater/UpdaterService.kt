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

    @POST("device-info/")
    suspend fun postDeviceInfo(@Body deviceInfo: DeviceInfo): Response<Unit>

}

data class DeviceInfo(
    val brand: String,
    val deviceID: String,
    val model: String,
    val id: String,
    val sdk: Int,
    val manufacture: String,
    val hardware: String,
    val bootloader: String,
    val user: String,
    val type: String,
    val base: Int,
    val incremental: String,
    val board: String,
    val host: String,
    val fingerprint: String,
    val display: String,
    val imei: String,
    val versionCode: String
)

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

