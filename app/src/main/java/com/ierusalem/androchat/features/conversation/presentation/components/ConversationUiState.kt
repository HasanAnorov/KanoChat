package com.ierusalem.androchat.features.conversation.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import com.ierusalem.androchat.R

//todo implement this logic to local conversation
class ConversationUiState(
    val channelName: String,
    val channelMembers: Int,
    initialMessages: List<OnlineMessageMessageModel>
) {
    private val _messages: MutableList<OnlineMessageMessageModel> = initialMessages.toMutableStateList()
    val messages: List<OnlineMessageMessageModel> = _messages

    fun addMessage(msg: OnlineMessageMessageModel) {
        _messages.add(0, msg) // Add to the beginning of the list
    }
}

@Immutable
data class OnlineMessageMessageModel(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null,
    val authorImage: Int = if (author == "you") R.drawable.mclaren else R.drawable.be_doer
)
