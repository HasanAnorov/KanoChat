package com.ierusalem.androchat.features_tcp.tcp_chat.data.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage

class Converters {

    @TypeConverter
    fun fromChatMessageList(value: List<ChatMessage>): String {
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toChatMessageList(value: String): List<ChatMessage> {
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(value, type)
    }
}
