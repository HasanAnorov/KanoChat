package com.ierusalem.androchat.features_tcp.tcp_chat.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage

object ChatMessageTypeConverter {
    @TypeConverter
    fun fromChatMessageList(value: List<ChatMessage>): String {
        val gson = Gson()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toChatMessageList(value: String): List<ChatMessage> {
        val gson = Gson()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(value, type)
    }
}
