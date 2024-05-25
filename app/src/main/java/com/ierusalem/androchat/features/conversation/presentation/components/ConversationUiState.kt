package com.ierusalem.androchat.features.conversation.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import com.ierusalem.androchat.R

class ConversationUiState(
    val channelName: String,
    val channelMembers: Int,
    initialMessages: List<Message>
) {
    private val _messages: MutableList<Message> = initialMessages.toMutableStateList()
    val messages: List<Message> = _messages

    fun addMessage(msg: Message) {
        _messages.add(0, msg) // Add to the beginning of the list
    }
}

@Immutable
data class Message(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null,
    val authorImage: Int = if (author == "you") R.drawable.mclaren else R.drawable.be_doer
)
