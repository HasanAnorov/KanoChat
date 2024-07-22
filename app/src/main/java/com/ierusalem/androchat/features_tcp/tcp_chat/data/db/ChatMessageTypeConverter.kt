package com.ierusalem.androchat.features_tcp.tcp_chat.data.db

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity

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
    fun fromContactMessageEntity(contactMessageEntity: ChatMessageEntity.ContactMessageEntity): String {
        val type = object : TypeToken<ChatMessageEntity.ContactMessageEntity>() {}.type
        return gson.toJson(contactMessageEntity, type)
    }

    @TypeConverter
    fun toContactMessageEntity(contactMessageEntity: String): ChatMessageEntity.ContactMessageEntity {
        val type = object : TypeToken<ChatMessageEntity.ContactMessageEntity>() {}.type
        return gson.fromJson(contactMessageEntity, type)
    }

    @TypeConverter
    fun fromFileMessageEntity(fileMessageEntity: ChatMessageEntity.FileMessage): String {
        val type = object : TypeToken<ChatMessageEntity.FileMessage>() {}.type
        return gson.toJson(fileMessageEntity, type)
    }

    @TypeConverter
    fun toFileMessageEntity(fileMessageEntity: String): ChatMessageEntity.FileMessage {
        val type = object : TypeToken<ChatMessageEntity.FileMessage>() {}.type
        return gson.fromJson(fileMessageEntity, type)
    }

}
