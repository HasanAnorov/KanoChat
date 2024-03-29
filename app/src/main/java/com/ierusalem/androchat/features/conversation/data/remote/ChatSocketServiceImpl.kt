package com.ierusalem.androchat.features.conversation.data.remote

import android.util.Log
import com.ierusalem.androchat.features.auth.register.data.remote.dto.MessageDto
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.utils.Resource
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class ChatSocketServiceImpl(
    private val client: HttpClient
) : ChatSocketService {

    //we will use this to send something into this connection
    private var socket: WebSocketSession? = null

    override suspend fun initSession(username: String): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                url(
                    urlString ="${ChatSocketService.Endpoints.ChatSocket.url}?username=$username"
                )
            }
            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else Resource.Failure(errorMessage = "Could not establish a connection")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(errorMessage = e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun sendMessage(message: String) {
        try {
            socket?.send(Frame.Text(message))
        }catch (e:Exception){
            Log.d("ahi3646_catch", "sendMessage: ${e.localizedMessage} ")
            e.printStackTrace()
        }
    }

    override fun observerMessages(): Flow<Message> {
        return try {
            socket?.incoming
                ?.receiveAsFlow()
                ?.filter { it is Frame.Text }
                ?.map { frame ->
                    val json =(frame as? Frame.Text)?.readText() ?: ""
                    val messageDto = Json.decodeFromString<MessageDto>(json)
                    messageDto.toMessage()
                } ?: flow {  }
        }catch (e: Exception){
            e.printStackTrace()
            flow {  }
        }
    }

    override suspend fun closeSession() {
        socket?.close()
    }
}