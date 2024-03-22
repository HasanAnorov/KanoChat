package com.ierusalem.androchat.app

import com.ierusalem.androchat.features.auth.register.data.remote.MessageService
import com.ierusalem.androchat.features.auth.register.data.remote.MessageServiceImpl
import com.ierusalem.androchat.features.conversation.data.remote.ChatSocketService
import com.ierusalem.androchat.features.conversation.data.remote.ChatSocketServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient{
        return HttpClient(CIO){
            install(Logging)
            install(WebSockets)
            install(JsonFeature){
                serializer = KotlinxSerializer()
            }
        }
    }

    @Provides
    @Singleton
    fun provideMessageService(client: HttpClient): MessageService{
        return MessageServiceImpl(client = client)
    }

    @Provides
    @Singleton
    fun provideChatSocketService(client: HttpClient): ChatSocketService{
        return ChatSocketServiceImpl(client = client)
    }

}