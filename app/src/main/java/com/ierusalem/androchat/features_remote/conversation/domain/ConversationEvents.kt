package com.ierusalem.androchat.features_remote.conversation.domain

sealed class ConversationEvents {
    data class SendMessage(val message: String): ConversationEvents()
    data object NavIconClick: ConversationEvents()
    data object NavigateToProfile: ConversationEvents()
}