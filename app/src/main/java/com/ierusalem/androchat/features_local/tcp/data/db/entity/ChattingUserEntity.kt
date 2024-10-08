package com.ierusalem.androchat.features_local.tcp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser

@Entity("chatting_users")
data class ChattingUserEntity(
    @PrimaryKey val partnerSessionID: String,
    val partnerUsername: String,
    val authorSessionId:String,
    val avatarBackgroundColor: Int,
    val isOnline: Boolean,
    val createdAt: String
){
    fun toChattingUser(): ChattingUser{
        return ChattingUser(
            partnerSessionID = partnerSessionID,
            partnerUsername = partnerUsername,
            avatarBackgroundColor = avatarBackgroundColor,
            lastMessage = null,
            isOnline = isOnline
        )
    }
}

data class UserWithLastMessage(
    val partnerSessionID: String,
    val partnerUsername: String,
    val avatarBackgroundColor: Int,
    val isOnline: Boolean,
    @Embedded val lastMessage: ChatMessageEntity?
){
    fun toChattingUser(): ChattingUser{
        return ChattingUser(
            partnerSessionID = partnerSessionID,
            partnerUsername = partnerUsername,
            avatarBackgroundColor = avatarBackgroundColor,
            lastMessage = lastMessage,
            isOnline = isOnline
        )
    }
}