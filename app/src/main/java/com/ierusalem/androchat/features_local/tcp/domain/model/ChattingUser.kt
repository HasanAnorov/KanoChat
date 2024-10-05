package com.ierusalem.androchat.features_local.tcp.domain.model

import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.state.InitialUserModel

data class ChattingUser(
    var isOnline:Boolean,
    val partnerSessionID: String,
    val partnerUsername: String,
    val avatarBackgroundColor: Int,
    val lastMessage: ChatMessageEntity?
){
    fun toInitialChatModel() = InitialUserModel(
        partnerSessionId = partnerSessionID,
        partnerUniqueName = partnerUsername
    )
}