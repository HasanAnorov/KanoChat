package com.ierusalem.androchat.features_tcp.tcp.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.features_tcp.tcp.data.db.entity.ChattingUserEntity

class ChattingUserConverter {

    @TypeConverter
    fun fromChatUserEntity(value: ChattingUserEntity): String {
        val type = object : TypeToken<ChattingUserEntity>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toChatUserEntity(value: String): ChattingUserEntity {
        val type = object : TypeToken<ChattingUserEntity>() {}.type
        return gson.fromJson(value, type)
    }

}