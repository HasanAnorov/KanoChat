package com.ierusalem.androchat.features.auth.register.data.remote

import com.ierusalem.androchat.features.auth.register.domain.model.Message

interface MessageService {
    suspend fun getAllMessages(): List<Message>

    companion object{
        //todo setup right by ipconfig
        const val BASE_URL = "http://10.0.2.2:8080"
    }

    sealed class Endpoints(val url: String){
        data object GetAllMessages: Endpoints(url = "$BASE_URL/messages")
    }
}