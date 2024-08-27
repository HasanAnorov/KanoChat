package com.ierusalem.androchat.features_local.tcp_chat.data.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.FileMessageState

class ChatMessageTypeConverter {

    @TypeConverter
    fun fromChatMessageEntity(value: ChatMessageEntity): String {
        val type = object : TypeToken<ChatMessageEntity>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toChatMessageEntity(value: String): ChatMessageEntity {
        val type = object : TypeToken<ChatMessageEntity>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFileState(value: FileMessageState): String {
        val type = object : TypeToken<FileMessageState>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toFileState(value: String): FileMessageState {
        val type = object : TypeToken<FileMessageState>() {}.type
        return gson.fromJson(value, type)
    }

}
