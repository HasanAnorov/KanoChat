package com.ierusalem.androchat.features.conversation.domain

sealed class ConversationEvents {
    data class SendMessage(val message: String): ConversationEvents()
    data object NavIconClick: ConversationEvents()
    data object NavigateToProfile: ConversationEvents()
}