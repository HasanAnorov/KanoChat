package com.ierusalem.androchat.features_local.tcp.domain.model

data class ChattingUser(
    val isOnline:Boolean,
    val userUniqueId: String,
    val username: String,
    val avatarBackgroundColor: Int
)