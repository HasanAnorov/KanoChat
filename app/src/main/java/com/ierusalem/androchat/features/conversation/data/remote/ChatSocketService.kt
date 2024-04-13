package com.ierusalem.androchat.features.conversation.data.remote

import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features.fcm.SendMessageDto
import com.ierusalem.androchat.utils.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatSocketService {

    suspend fun initSession(
        username: String
    ): Resource<Unit>

//    web socket version
    suspend fun sendMessage(message: String)

    @POST("/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto
    )

    @POST("/broadcast")
    suspend fun broadcast(
        @Body body: SendMessageDto
    )

    fun observerMessages(): Flow<Message>

    suspend fun closeSession()

    companion object{
        const val BASE_URL = "ws://10.0.2.2:8080"
    }

    sealed class Endpoints(val url: String){
        data object ChatSocket: Endpoints(url = "$BASE_URL/chat-socket")
    }
}