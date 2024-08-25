package com.ierusalem.androchat.features_tcp.tcp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("chatting_users")
data class ChattingUserEntity(
    @PrimaryKey val userUniqueId: String,
    val userUniqueName: String
)