package com.ierusalem.androchat.features.conversation.data.remote

import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {

    suspend fun initSession(
        username: String
    ): Resource<Unit>

    suspend fun sendMessage(message: String)

    fun observerMessages(): Flow<Message>

    suspend fun closeSession()

    companion object{
        const val BASE_URL = "ws://10.0.2.2:8080"
    }

    sealed class Endpoints(val url: String){
        data object ChatSocket: Endpoints(url = "$BASE_URL/chat-socket")
    }
}