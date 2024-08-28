package com.ierusalem.androchat.features_remote.conversation.domain

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.features_local.tcp_conversation.data.db.entity.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    NavigationEventDelegate<ConversationNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<ConversationState> = MutableStateFlow(
        ConversationState()
    )
    val state = _state.asStateFlow()

    fun handleIntents(event: ConversationEvents) {
        when (event) {
            is ConversationEvents.SendMessage -> {
                Log.d("ahi3646", "handleIntents: send message ${event.message} ")
                //sendMessage(event.message)
            }
            ConversationEvents.NavigateToProfile -> {}
            ConversationEvents.NavIconClick -> {
                emitNavigation(ConversationNavigation.OnNavIconClick)
            }
        }
    }

    fun connectToChat(username: String) {
        Log.d("ahi3646", "handleIntents: send message ${savedStateHandle.get<String>("username")} ")
    }


}

data class ConversationState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface ConversationNavigation{
    data object OnNavIconClick: ConversationNavigation
}