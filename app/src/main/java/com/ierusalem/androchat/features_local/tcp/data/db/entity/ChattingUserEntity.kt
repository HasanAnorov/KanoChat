package com.ierusalem.androchat.features_local.tcp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser


@Entity("chatting_users")
data class ChattingUserEntity(
    @PrimaryKey val userUniqueId: String,
    val userUniqueName: String,
    val avatarBackgroundColor: Int,
) {
    fun toChattingUser(): ChattingUser {
        return ChattingUser(
            isOnline = false,
            userUniqueId = userUniqueId,
            username = userUniqueName,
            avatarBackgroundColor = avatarBackgroundColor
        )
    }
}