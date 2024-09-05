package com.ierusalem.androchat.features_local.tcp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser

@Entity("chatting_users")
data class ChattingUserEntity(
    @PrimaryKey val userUniqueId: String,
    val userUniqueName: String,
    val avatarBackgroundColor: Int,
    val isOnline: Boolean,
)

data class UserWithLastMessage(
    val userUniqueId: String,
    val userUniqueName: String,
    val avatarBackgroundColor: Int,
    val isOnline: Boolean,
    @Embedded val lastMessage: ChatMessageEntity?
){
    fun toChattingUser(): ChattingUser{
        return ChattingUser(
            userUniqueId = userUniqueId,
            username = userUniqueName,
            avatarBackgroundColor = avatarBackgroundColor,
            lastMessage = lastMessage,
            isOnline = isOnline
        )
    }
}