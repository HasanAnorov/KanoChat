package com.ierusalem.androchat.features.auth.register.data.remote

import com.ierusalem.androchat.features.auth.register.data.remote.dto.MessageDto
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.*

class MessageServiceImpl(
    private val client: HttpClient
) : MessageService {
    override suspend fun getAllMessages(): List<Message> {
        return try {
            client.get<List<MessageDto>>(MessageService.Endpoints.GetAllMessages.url)
                .map { it.toMessage() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}