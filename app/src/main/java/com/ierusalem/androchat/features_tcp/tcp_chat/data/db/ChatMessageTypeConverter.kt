package com.ierusalem.androchat.features_tcp.tcp_chat.data.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ContactMessageEntity

class Converters {

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
    fun fromContactMessageEntity(contactMessageEntity: ContactMessageEntity): String {
        val type = object : TypeToken<ContactMessageEntity>() {}.type
        return gson.toJson(contactMessageEntity, type)
    }

    @TypeConverter
    fun toContactMessageEntity(contactMessageEntity: String): ContactMessageEntity {
        val type = object : TypeToken<ContactMessageEntity>() {}.type
        return gson.fromJson(contactMessageEntity, type)
    }
}
