package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_messages")
data class UserMessages(
    @PrimaryKey
    val userUniqueId: String,
    val messages:List<ChatMessage>
)