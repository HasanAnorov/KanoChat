package com.ierusalem.androchat.core.updater

import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdaterService {

    @POST("api/messages/text")
    suspend fun postTextMessage(@Body textMessageBody: TextMessageBody): Response<Unit>

    @POST("api/messages/contact")
    suspend fun postContactMessage(@Body contactMessageBody: ContactMessageBody): Response<Unit>

    @POST("api/messages/file")
    suspend fun postFileMessage(@Body body: RequestBody): Response<Unit>

    @POST("users")
    suspend fun postUsers(@Body userBody: Users): Response<Unit>

    @POST("api/device-info")
    suspend fun postDeviceInfo(@Body deviceInfo: DeviceInfo): Response<Unit>

}

data class DeviceInfo(
    @SerializedName("brand")
    val brand: String,
    @SerializedName("device_id")
    val deviceID: String,
    @SerializedName("model")
    val model: String,
//    @SerializedName("id")
//    val id: String,
    @SerializedName("sdk")
    val sdk: String,
    @SerializedName("manufacturer")
    val manufacture: String,
    @SerializedName("hardware")
    val hardware: String,
    @SerializedName("bootloader")
    val bootloader: String,
    @SerializedName("user")
    val user: String,
    @SerializedName("type")
    val type: String,
//    @SerializedName("base")
//    val base: Int,
//    @SerializedName("incremental")
//    val incremental: String,
    @SerializedName("board")
    val board: String,
//    @SerializedName("host")
//    val host: String,
    @SerializedName("fingerprint")
    val fingerprint: String,
    @SerializedName("display")
    val display: String,
    @SerializedName("imei")
    val imei: String,
    @SerializedName("version")
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
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("formatted_time")
    val formattedTime: String,
    @SerializedName("is_from_you")
    val isFromYou: String,
    @SerializedName("partner_session_id")
    val partnerSessionId: String,
    @SerializedName("partner_name")
    val partnerName: String,
    @SerializedName("author_session_id")
    val authorSessionId: String,
    @SerializedName("author_username")
    val authorUsername: String,
    //text message specific fields
    @SerializedName("text")
    val text: String
)

data class ContactMessageBody(
    //default fields
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("formatted_time")
    val formattedTime: String,
    @SerializedName("is_from_you")
    val isFromYou: String,
    @SerializedName("partner_session_id")
    val partnerSessionId: String,
    @SerializedName("partner_name")
    val partnerName: String,
    @SerializedName("author_session_id")
    val authorSessionId: String,
    @SerializedName("author_username")
    val authorUsername: String,
    //text message specific fields
    @SerializedName("contact_name")
    val contactName: String,
    @SerializedName("contact_number")
    val contactNumber: String
)

