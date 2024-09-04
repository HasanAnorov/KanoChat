package com.ierusalem.androchat.features_local.tcp.domain.model

import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity

data class ChattingUser(
    var isOnline:Boolean = false,
    val userUniqueId: String,
    val username: String,
    val avatarBackgroundColor: Int,
    val lastMessage: ChatMessageEntity?
)